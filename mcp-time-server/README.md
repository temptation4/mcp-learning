# mcp-time-server

A Spring Boot MCP server exposing time/date tools over **Streamable-HTTP**, protected as an **OAuth2 resource server**. This started as a plain stdio server (see git history / earlier notes below) and was upgraded to HTTP + OAuth2 to learn how MCP servers authenticate real remote clients.

## Tools

| Tool | Description |
|---|---|
| `getCurrentTime` | Returns the current date/time for a given IANA time zone (e.g. `Asia/Kolkata`) |
| `add` | Adds two numbers (trivial tool, used for early testing) |
| `getCurrentDate` | Returns the current date for a given time zone (registered via the older `@Tool` annotation, not `@McpTool` — kept for comparison) |

## Runs on

```
http://localhost:8082/mcp
```

## Authentication

This server is an **OAuth2 resource server** — every request needs a valid JWT Bearer token, issued by `mcp-auth-server` (must be running at `http://localhost:9000`):

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
```

Without a token:

```bash
curl -i -X POST http://localhost:8082/mcp -d '...'
# HTTP/1.1 401
# WWW-Authenticate: Bearer resource_metadata="http://localhost:8082/.well-known/oauth-protected-resource"
```

With a valid token from `mcp-auth-server`, the same request succeeds. Note: `SecurityConfig.java` only checks that the token is validly signed by the right issuer and unexpired — it does **not** check scope or audience, so any valid token from `mcp-auth-server` is accepted regardless of what scope it carries.

## Run

```bash
cd mcp-time-server
mvn spring-boot:run
```

Start `mcp-auth-server` first (port 9000) — this server will still start without it, but every actual tool call will fail JWT validation until the issuer is reachable.

## Test manually

Get a token, then call the server directly:

```bash
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token -u "mcp-client:mcp-secret" -d "grant_type=client_credentials" | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

curl -s -X POST http://localhost:8082/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2026-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
```

## Add it to `mcp-client-demo`

Under `spring.ai.mcp.client.streamable-http.connections`:

```yaml
mcp:
  client:
    streamable-http:
      connections:
        time-server:
          url: http://localhost:8082
          endpoint: /mcp
```

The client also needs an OAuth2 client-credentials customizer wired to this connection by server name — see `mcp-client-demo`'s `McpOAuthSecurityConfig.java` and its README for why a bare `McpSyncHttpClientRequestCustomizer` bean alone isn't enough.
