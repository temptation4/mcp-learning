package com.learn.mcpfileserver;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class SafeFilePath {

    private static final Path BASE_DIR =
            Path.of("/Users/neelu/Desktop/mcp-demo")
                    .toAbsolutePath()
                    .normalize();

    public Path validate(String requestedPath) {

        Path path = Path.of(requestedPath)
                .toAbsolutePath()
                .normalize();

        if (!path.startsWith(BASE_DIR)) {
            throw new IllegalArgumentException(
                    "Access denied. File must be inside: " + BASE_DIR
            );
        }

        return path;
    }

    public Path getBaseDir() {
        return BASE_DIR;
    }
}