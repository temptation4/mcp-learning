package com.learn.mcpfileserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileResources {

    private static final Logger log =
            LoggerFactory.getLogger(FileResources.class);

    private static final Path BASE_DIR =
            Path.of("/Users/neelu/Desktop/mcp-demo")
                    .toAbsolutePath()
                    .normalize();

    @McpResource(
            uri = "file://mcp-demo/{name}",
            name = "MCP File",
            description = "Reads a text file from the MCP demo directory",
            mimeType = "text/plain"
    )
    public String getFile(String name) {

        log.info("getFile resource called with name={}", name);

        Path file = BASE_DIR.resolve(name)
                .normalize();

        log.info("Resolved resource path={}", file);

        if (!file.startsWith(BASE_DIR)) {
            log.warn("Blocked access outside base directory: {}", file);
            throw new IllegalArgumentException(
                    "Access outside the allowed directory is not permitted"
            );
        }

        try {
            String content = Files.readString(file);

            log.info(
                    "Resource read successfully, file={}, contentLength={}",
                    file,
                    content.length()
            );

            return content;

        } catch (IOException e) {

            log.error(
                    "Failed to read resource file={}",
                    file,
                    e
            );

            throw new IllegalStateException(
                    "Could not read resource: " + name,
                    e
            );
        }
    }


    @McpResource(
            uri = "file://mcp-demo/readme",
            name = "MCP Readme",
            description = "Provides the readme content for the MCP file server",
            mimeType = "text/plain"
    )
    public String getReadme() {

        log.info("getReadme resource called");

        Path file = BASE_DIR
                .resolve("readme.txt")
                .normalize();

        try {
            String content = Files.readString(file);

            log.info(
                    "Readme resource read successfully, contentLength={}",
                    content.length()
            );

            return content;

        } catch (IOException e) {

            log.error(
                    "Failed to read readme resource",
                    e
            );

            throw new IllegalStateException(
                    "Could not read readme resource",
                    e
            );
        }
    }
}