# mcp-file-server

A Spring Boot MCP server exposing file operations as tools, over **stdio** transport. This was the first server built in this learning series — a local MCP client (like Claude Desktop) launches it as a subprocess and talks to it over stdin/stdout.

## Tools

| Tool | Description |
|---|---|
| `readFile` | Reads and returns the text contents of a file |
| `listFiles` | Lists files in a directory, filtered by extension |
| `writeFile` | Creates or overwrites a text file with given content |
| `renameFile` | Renames/moves a file |
| `copyFile` | Copies a file from one path to another |
| `deleteFile` | Deletes a file |
| `showClientRoots` | Returns the filesystem roots the connecting MCP client exposes |
| `add` | Adds two numbers (a trivial tool, used for early testing) |

## Resources

| URI | Description |
|---|---|
| `file://mcp-demo/{name}` | Reads any file inside the sandboxed demo directory by name |
| `file://mcp-demo/readme` | Reads `readme.txt` from the sandboxed demo directory |

## Security: sandboxed to one directory

Early on, this server had **no path restriction at all** — any tool call could read/write/delete any file the local user account could touch (SSH keys, `.env` files, anything). `SafeFilePath.java` now restricts every tool and resource to one directory:

```
/Users/neelu/Desktop/mcp-demo
```

Any path outside that directory is rejected with `IllegalArgumentException`. If you point a tool call outside this directory, expect `"Access denied: ..."` in the response, not a real read/write.

## Why logging goes to a file, not the console

This is a **stdio** MCP server — its stdout *is* the JSON-RPC protocol stream. Any stray console output (Spring Boot's banner, a log line) corrupts every message a client tries to parse. `logback-spring.xml` removes the console appender entirely and writes logs to:

```
~/Documents/code/mcp-learning/mcp-file-server/mcp-file-server.log
```

The path is absolute (not relative) for a specific reason: when Claude Desktop launches this jar, it runs from a working directory that turned out to be **read-only** (macOS app sandboxing) — a relative log path crashed the whole app on startup before it ever got to speak MCP. Use an absolute path if you fork this for your own project.

## Build

```bash
cd mcp-file-server
mvn clean package -DskipTests
```

Jar lands at `target/mcp-file-server-0.0.1-SNAPSHOT.jar`.

## Run standalone (for manual protocol testing)

A stdio server has no port to curl. To test it by hand, keep stdin open across multiple requests (closing it early makes the server think the client disconnected mid-response):

```bash
mkfifo /tmp/mcp_fifo
java -jar target/mcp-file-server-0.0.1-SNAPSHOT.jar < /tmp/mcp_fifo &
exec 3>/tmp/mcp_fifo
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2026-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' >&3
# ... more requests ...
exec 3>&-
```

## Connect it to a real client

**Claude Desktop** (`~/Library/Application Support/Claude/claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "file-reader": {
      "command": "/path/to/java",
      "args": ["-jar", "/absolute/path/to/mcp-file-server-0.0.1-SNAPSHOT.jar"]
    }
  }
}
```

Use an absolute path to `java` (not just `"java"`) — GUI apps on macOS often launch with a minimal `PATH` that doesn't include your shell's.

**mcp-client-demo** (this repo's own client project) connects to it via `spring.ai.mcp.client.stdio.connections.file-server` in its `application.yml`.
