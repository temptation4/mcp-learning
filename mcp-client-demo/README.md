# mcp-client-demo

The client side of this MCP learning series. A single Spring Boot app that connects to **three different MCP servers** over two different transports, one of them OAuth2-secured, and wires the discovered tools into both raw REST endpoints and a real `ChatClient` so an LLM can call them.

## What it connects to

| Server | Transport | Auth |
|---|---|---|
| `file-server` (`mcp-file-server`) | stdio (launched as a subprocess) | none |
| `time-server` (`mcp-time-server`) | Streamable-HTTP, `localhost:8082` | OAuth2 client-credentials |
| `database-server` (`mcp-database-server`) | Streamable-HTTP, `localhost:8083` | none |

Configured in `application.yml` under `spring.ai.mcp.client.stdio.connections` / `spring.ai.mcp.client.streamable-http.connections`.

Runs on `http://localhost:8091`.

## Prerequisites

Build `mcp-file-server` first (this client launches it as a subprocess):

```bash
cd ../mcp-file-server && mvn clean package -DskipTests
```

Start these separately, in this order, before running this client:

1. `mcp-auth-server` (port 9000) — needed for the `time-server` connection to authenticate
2. `mcp-time-server` (port 8082)
3. `mcp-database-server` (port 8083) — needs MySQL running with the configured credentials
4. Ollama running locally (`llama3.2` pulled) — needed for the `/chat` endpoint

Then run this project:

```bash
mvn spring-boot:run
```

## The OAuth2 wiring gotcha (worth understanding, not just copying)

Spring AI's autoconfiguration is *supposed* to auto-detect a bare `McpSyncHttpClientRequestCustomizer` bean and apply it to every HTTP-based MCP connection. In practice, that auto-detection never actually invoked the customizer — confirmed by adding DEBUG logging and watching its own `"Requesting access token"` line never appear. Every request to `time-server` went out with no `Authorization` header, and got `401`.

The fix in `McpOAuthSecurityConfig.java`: wire the OAuth2 customizer **explicitly** via `McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>`, filtered by server name:

```java
@Bean
McpClientCustomizer<HttpClientStreamableHttpTransport.Builder> oauth2HttpCustomizer(
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {

    McpSyncHttpClientRequestCustomizer oauth2RequestCustomizer =
            new OAuth2ClientCredentialsSyncHttpRequestCustomizer(
                    authorizedClientManager, "authserver-client-credentials");

    return (serverName, builder) -> {
        if (!"time-server".equals(serverName)) return;
        builder.httpRequestCustomizer(oauth2RequestCustomizer);
    };
}
```

If you add a fourth OAuth2-protected server later, extend this same `if` check rather than assuming the automatic path will pick it up.

## Endpoints

### `POST /chat` — talk to an LLM with all discovered tools available

```bash
curl -X POST http://localhost:8091/chat -H "Content-Type: text/plain" -d "What's the current time in Tokyo?"
```

`ChatConfig.java` wires every discovered MCP tool into the `ChatClient` **except** `deleteFile` — that one's deliberately excluded from LLM access (see `DeleteController` below for why).

### `GET /mcp/tools` — list every tool discovered across all three servers

```bash
curl http://localhost:8091/mcp/tools
```

### `POST /mcp/read?path=...` — call `readFile` directly (bypassing the LLM)

### `POST /mcp/add-time?a=1&b=2` — call time-server's `add` tool directly

### `GET /mcp/roots` — call `showClientRoots`, confirming the client-side roots capability (see below)

### `POST /delete/request?path=...` then `POST /delete/confirm?confirmationId=...` — two-step delete

`deleteFile` is excluded from the LLM's tool list specifically so an LLM can never delete a file on its own initiative. This REST-only, two-step confirmation flow is the only way to actually delete something — request it, get a confirmation ID back, then confirm with that exact ID. This is a deliberate design lesson: a *destructive* tool doesn't have to be exposed to the LLM just because the server offers it.

## MCP Roots

`McpRootsConfig.java` tells `file-server` which directory this client considers its "root" (`file:///Users/neelu/Desktop/mcp-demo`) — a client-side capability, separate from the server's own `SafeFilePath` sandboxing. `showClientRoots` on the server calls back to the client to ask what roots it's declared.
