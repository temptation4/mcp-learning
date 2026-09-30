# Java MCP Filesystem Server

A Java/Spring AI implementation of an MCP filesystem server using **STDIO** transport.

This project is intentionally separate from the original `mcp-file-server` in this repository. The original project remains available as a learning implementation.

## Features

MCP tools currently exposed:

| Tool | Purpose |
|---|---|
| `read_file` | Read UTF-8 text from a file |
| `write_file` | Create or overwrite a text file |
| `list_directory` | List immediate directory children |
| `create_directory` | Create directories |
| `move_file` | Move or rename a file |
| `search_files` | Recursively search filenames |
| `get_file_info` | Read basic file metadata |
| `list_allowed_directories` | Show configured filesystem roots |

## Security model

The server does **not** expose the whole host filesystem.

Access is limited to directories supplied through:

`MCP_FILESYSTEM_ALLOWED_DIRECTORIES`

Multiple directories can be separated by commas.

Example:

```bash
export MCP_FILESYSTEM_ALLOWED_DIRECTORIES="/Users/neelu/Documents/code,/tmp/mcp-demo"
```

Every tool validates the normalized absolute path and requires it to start inside one of the configured roots.

For production use, add further hardening around symlinks, file size limits, binary files, permissions, and destructive operations.

## Build

```bash
mvn clean package
```

## Run as an MCP STDIO server

STDIO servers should normally be launched by an MCP client. Do not expect a normal terminal prompt.

Example:

```bash
export MCP_FILESYSTEM_ALLOWED_DIRECTORIES="/Users/neelu/Documents/code"
java -jar target/java-mcp-filesystem-server-0.1.0.jar
```

The process waits for MCP JSON-RPC messages on stdin/stdout.

## Spring AI client configuration

Example:

```yaml
spring:
  ai:
    mcp:
      client:
        type: SYNC
        stdio:
          connections:
            java-filesystem:
              command: java
              args:
                - "-jar"
                - "/absolute/path/to/java-mcp-filesystem-server-0.1.0.jar"
```

Set the environment variable for the process launched by your MCP client.

## Official filesystem comparison

This is an independent Java implementation. It is not a copy of the official TypeScript filesystem server.

The goal is to provide a Java/Spring AI implementation for developers who want to study or use MCP filesystem tools in a Java ecosystem.

## License

Apache License 2.0.
