package com.learn.mcpfilesystem.tools;

import com.learn.mcpfilesystem.security.SafePathService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FilesystemTools {

    private final SafePathService safePathService;

    public FilesystemTools(SafePathService safePathService) {
        this.safePathService = safePathService;
    }

    @McpTool(
            name = "read_file",
            description = "Read UTF-8 text from a file inside an allowed directory",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public String readFile(
            @McpToolParam(required = true, description = "Absolute file path") String path) {
        try {
            Path file = safePathService.validate(path);
            if (!Files.isRegularFile(file)) {
                return "Not a regular file: " + file;
            }
            return Files.readString(file);
        }
        catch (IllegalArgumentException e) {
            return "Access denied: " + e.getMessage();
        }
        catch (IOException e) {
            return "Could not read file: " + e.getMessage();
        }
    }

    @McpTool(
            name = "write_file",
            description = "Create or overwrite a UTF-8 text file inside an allowed directory",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = true,
                    openWorldHint = false))
    public String writeFile(
            @McpToolParam(required = true, description = "Absolute file path") String path,
            @McpToolParam(required = true, description = "Text content") String content) {
        try {
            Path file = safePathService.validate(path);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, content);
            return "File written successfully: " + file;
        }
        catch (IllegalArgumentException e) {
            return "Access denied: " + e.getMessage();
        }
        catch (IOException e) {
            return "Could not write file: " + e.getMessage();
        }
    }

    @McpTool(
            name = "list_directory",
            description = "List immediate children of a directory inside an allowed directory",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<String> listDirectory(
            @McpToolParam(required = true, description = "Absolute directory path") String path) {
        try {
            Path directory = safePathService.validate(path);
            if (!Files.isDirectory(directory)) {
                return List.of("Not a directory: " + directory);
            }

            try (Stream<Path> stream = Files.list(directory)) {
                return stream
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .map(Path::toString)
                        .toList();
            }
        }
        catch (IllegalArgumentException e) {
            return List.of("Access denied: " + e.getMessage());
        }
        catch (IOException e) {
            return List.of("Could not list directory: " + e.getMessage());
        }
    }

    @McpTool(
            name = "create_directory",
            description = "Create a directory and its missing parent directories",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public String createDirectory(
            @McpToolParam(required = true, description = "Absolute directory path") String path) {
        try {
            Path directory = safePathService.validate(path);
            Files.createDirectories(directory);
            return "Directory created: " + directory;
        }
        catch (IllegalArgumentException e) {
            return "Access denied: " + e.getMessage();
        }
        catch (IOException e) {
            return "Could not create directory: " + e.getMessage();
        }
    }

    @McpTool(
            name = "move_file",
            description = "Move or rename a file inside the allowed directories",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = false,
                    openWorldHint = false))
    public String moveFile(
            @McpToolParam(required = true, description = "Absolute source path") String source,
            @McpToolParam(required = true, description = "Absolute target path") String target) {
        try {
            Path sourcePath = safePathService.validate(source);
            Path targetPath = safePathService.validate(target);

            if (!Files.exists(sourcePath)) {
                return "Source does not exist: " + sourcePath;
            }
            if (Files.exists(targetPath)) {
                return "Target already exists: " + targetPath;
            }

            Path parent = targetPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.move(sourcePath, targetPath);
            return "Moved: " + sourcePath + " -> " + targetPath;
        }
        catch (IllegalArgumentException e) {
            return "Access denied: " + e.getMessage();
        }
        catch (IOException e) {
            return "Could not move path: " + e.getMessage();
        }
    }

    @McpTool(
            name = "search_files",
            description = "Search recursively for files whose names contain the given text",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<String> searchFiles(
            @McpToolParam(required = true, description = "Absolute directory path") String directory,
            @McpToolParam(required = true, description = "Case-insensitive filename fragment") String query) {
        try {
            Path root = safePathService.validate(directory);
            if (!Files.isDirectory(root)) {
                return List.of("Not a directory: " + root);
            }

            String normalized = query == null ? "" : query.toLowerCase();

            try (Stream<Path> stream = Files.walk(root)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase().contains(normalized))
                        .map(Path::toString)
                        .sorted()
                        .toList();
            }
        }
        catch (IllegalArgumentException e) {
            return List.of("Access denied: " + e.getMessage());
        }
        catch (IOException e) {
            return List.of("Could not search files: " + e.getMessage());
        }
    }

    @McpTool(
            name = "get_file_info",
            description = "Return basic metadata for a file or directory",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public String getFileInfo(
            @McpToolParam(required = true, description = "Absolute path") String path) {
        try {
            Path safePath = safePathService.validate(path);
            if (!Files.exists(safePath)) {
                return "Path does not exist: " + safePath;
            }

            return """
                    path=%s
                    type=%s
                    size=%d
                    lastModified=%s
                    """.formatted(
                    safePath,
                    Files.isDirectory(safePath) ? "directory" : "file",
                    Files.size(safePath),
                    Files.getLastModifiedTime(safePath));
        }
        catch (IllegalArgumentException e) {
            return "Access denied: " + e.getMessage();
        }
        catch (IOException e) {
            return "Could not read metadata: " + e.getMessage();
        }
    }

    @McpTool(
            name = "list_allowed_directories",
            description = "Return directories that this MCP server is configured to access",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<String> listAllowedDirectories() {
        return safePathService.getAllowedDirectories()
                .stream()
                .map(Path::toString)
                .toList();
    }
}
