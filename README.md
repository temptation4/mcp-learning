# mcp-learning

A hands-on Model Context Protocol (MCP) learning series, built progressively from a single stdio tool server to a multi-server system with OAuth2-secured HTTP transport, an LLM-facing `ChatClient`, and a real database-backed server. Five Spring Boot projects, each one adding a new MCP concept on top of the last.

## Projects, in the order they were built

| # | Project | What it adds |
|---|---|---|
| 1 | [`mcp-file-server`](mcp-file-server/) | The first MCP **server** — file read/write/list/delete tools over **stdio**. Started with zero access control (any file, readable by anyone); later sandboxed to one directory. |
| 2 | [`mcp-client-demo`](mcp-client-demo/) | The first MCP **client** — connects to `mcp-file-server` over stdio, exposes discovered tools via REST. |
| 3 | [`mcp-time-server`](mcp-time-server/) | A second server, upgraded from stdio to **Streamable-HTTP** — teaches connecting to multiple servers from one client. |
| 4 | [`mcp-auth-server`](mcp-auth-server/) | A real OAuth2 Authorization Server — issues client-credentials tokens so `mcp-time-server` can require real authentication instead of being wide open. |
| 5 | [`mcp-database-server`](mcp-database-server/) | A third server backed by a real MySQL database — read-only schema-inspection tools (`getTables`, `getTableSchema`), deliberately no arbitrary SQL execution. |

`mcp-client-demo` ties all of them together — it's the one client connecting to all three servers at once, one of them stdio, two of them HTTP, one of those two OAuth2-secured.

## Architecture

```
                    ┌─────────────────────┐
                    │   mcp-client-demo    │  :8091
                    │  (ChatClient + REST) │
                    └──────────┬───────────┘
                 ┌─────────────┼─────────────────┐
                 │ stdio       │ HTTP             │ HTTP + OAuth2
                 ▼             ▼                  ▼
        ┌────────────────┐ ┌──────────────────┐ ┌────────────────┐
        │ mcp-file-server │ │mcp-database-server│ │ mcp-time-server │  :8082
        │  (subprocess)   │ │      :8083        │ │                 │
        └─────────────────┘ └─────────┬─────────┘ └────────┬────────┘
                                       │                    │ validates JWT
                                       ▼                    ▼
                                    MySQL           ┌─────────────────┐
                                                     │  mcp-auth-server │  :9000
                                                     └─────────────────┘
```

## Run order

To bring up the full system:

```bash
# 1. Build the stdio server (mcp-client-demo launches this as a subprocess)
cd mcp-file-server && mvn clean package -DskipTests && cd ..

# 2. Start the auth server (needed before time-server can validate anything)
cd mcp-auth-server && mvn spring-boot:run &
cd ..

# 3. Start the time server (needs mcp-auth-server up)
cd mcp-time-server && mvn spring-boot:run &
cd ..

# 4. Start the database server (needs MySQL running, see its README)
cd mcp-database-server && mvn spring-boot:run &
cd ..

# 5. Start the client last (needs everything above)
cd mcp-client-demo && mvn spring-boot:run
```

Also needs Ollama running locally with `llama3.2` pulled, for `mcp-client-demo`'s `/chat` endpoint.

## The two lessons worth remembering from this series

1. **A tool with no access restriction is a real vulnerability, not a toy.** `mcp-file-server`'s `readFile` originally accepted any absolute path — meaning any MCP client (or anything that convinces an LLM to call it, like a prompt-injected webpage) could read `~/.ssh/id_rsa` just as easily as a demo file. `SafeFilePath.java` sandboxes every tool to one directory. Any new tool that touches the filesystem, a database, or another service should ask "what's the blast radius if this gets called with a value I didn't expect?" before it ships.

2. **Framework auto-detection isn't always as automatic as advertised.** Spring AI's autoconfiguration is supposed to auto-wire a plain `McpSyncHttpClientRequestCustomizer` bean into every HTTP MCP connection. It silently didn't — the OAuth2 flow to `mcp-time-server` failed with `401` for a long time before DEBUG logging revealed the customizer was never actually being invoked. The fix (explicit `McpClientCustomizer<Builder>` wiring, filtered by server name) is documented in `mcp-client-demo`'s README. When something that "should just work" doesn't, verify what's actually happening — check for `Authorization` header, add debug logging — rather than assuming the config is wrong.

## Individual project docs

Each project has its own README with tool lists, exact run commands, and (for the ones with a story) what changed and why:

- [mcp-file-server/README.md](mcp-file-server/README.md)
- [mcp-client-demo/README.md](mcp-client-demo/README.md)
- [mcp-time-server/README.md](mcp-time-server/README.md)
- [mcp-auth-server/README.md](mcp-auth-server/README.md)
- [mcp-database-server/README.md](mcp-database-server/README.md)
# -mcp-learning
