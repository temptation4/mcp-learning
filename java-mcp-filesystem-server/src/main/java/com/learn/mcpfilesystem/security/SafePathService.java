package com.learn.mcpfilesystem.security;

import com.learn.mcpfilesystem.config.FilesystemProperties;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class SafePathService {

    private final List<Path> allowedDirectories;

    public SafePathService(FilesystemProperties properties) {
        this.allowedDirectories = properties.allowedPaths();

        if (allowedDirectories.isEmpty()) {
            throw new IllegalStateException(
                    "No filesystem directories are configured. " +
                    "Set MCP_FILESYSTEM_ALLOWED_DIRECTORIES."
            );
        }

        for (Path directory : allowedDirectories) {
            if (!Files.isDirectory(directory)) {
                throw new IllegalStateException(
                        "Allowed directory does not exist or is not a directory: " + directory
                );
            }
        }
    }

    public Path validate(String requestedPath) {
        if (requestedPath == null || requestedPath.isBlank()) {
            throw new IllegalArgumentException("Path must not be blank");
        }

        Path requested = Path.of(requestedPath)
                .toAbsolutePath()
                .normalize();

        boolean allowed = allowedDirectories.stream()
                .anyMatch(requested::startsWith);

        if (!allowed) {
            throw new IllegalArgumentException(
                    "Access denied. Path must be inside one of the configured directories: "
                            + allowedDirectories
            );
        }

        return requested;
    }

    public List<Path> getAllowedDirectories() {
        return List.copyOf(allowedDirectories);
    }
}
