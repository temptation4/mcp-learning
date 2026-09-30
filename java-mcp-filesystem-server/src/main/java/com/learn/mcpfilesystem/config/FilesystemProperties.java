package com.learn.mcpfilesystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "mcp.filesystem")
public class FilesystemProperties {

    private List<String> allowedDirectories = List.of();

    public List<String> getAllowedDirectories() {
        return allowedDirectories;
    }

    public void setAllowedDirectories(List<String> allowedDirectories) {
        this.allowedDirectories = allowedDirectories;
    }

    public List<Path> allowedPaths() {
        return allowedDirectories.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> Path.of(value).toAbsolutePath().normalize())
                .toList();
    }
}
