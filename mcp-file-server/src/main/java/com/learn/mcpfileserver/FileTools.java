package com.learn.mcpfileserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class FileTools {

    private static final Logger log =
            LoggerFactory.getLogger(FileTools.class);

    private final SafeFilePath safeFilePath;

    public FileTools(SafeFilePath safeFilePath) {
        this.safeFilePath = safeFilePath;
    }

    // ============================================================
    // 1. READ FILE
    // ============================================================

    @McpTool(
            name = "readFile",
            description = "Reads and returns the text contents of a file given its absolute path",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public String readFile(
            @McpToolParam(
                    required = true,
                    description = "Absolute path to the file to read"
            )
            String path) {

        log.info("readFile called with path={}", path);

        try {

            Path safePath = safeFilePath.validate(path);

            if (!Files.exists(safePath)) {
                log.warn("File does not exist: {}", safePath);

                return "File does not exist: " + safePath;
            }

            if (!Files.isRegularFile(safePath)) {
                log.warn("Path is not a regular file: {}", safePath);

                return "Path is not a regular file: " + safePath;
            }

            String content = Files.readString(safePath);

            log.info(
                    "readFile successful, path={}, contentLength={}",
                    safePath,
                    content.length()
            );

            return content;

        } catch (IllegalArgumentException e) {

            log.warn(
                    "Access denied while reading file: {}",
                    e.getMessage()
            );

            return "Access denied: " + e.getMessage();

        } catch (IOException e) {

            log.error(
                    "Failed to read file, path={}",
                    path,
                    e
            );

            return "Could not read file: " + e.getMessage();
        }
    }


    // ============================================================
    // 2. LIST FILES
    // ============================================================

    @McpTool(
            name = "listFiles",
            description = "Lists files in a directory filtered by file extension",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public List<String> listFiles(
            @McpToolParam(
                    required = true,
                    description = "Absolute path of the directory"
            )
            String directory,

            @McpToolParam(
                    required = true,
                    description = "File extension to filter, for example .txt or .pdf"
            )
            String extension) {

        log.info("========== listFiles START ==========");
        log.info("directory={}", directory);
        log.info("extension={}", extension);

        try {

            Path safeDirectory =
                    safeFilePath.validate(directory);

            if (!Files.exists(safeDirectory)) {
                log.warn(
                        "Directory does not exist: {}",
                        safeDirectory
                );

                return List.of(
                        "Directory does not exist: "
                                + safeDirectory
                );
            }

            if (!Files.isDirectory(safeDirectory)) {
                log.warn(
                        "Path is not a directory: {}",
                        safeDirectory
                );

                return List.of(
                        "Path is not a directory: "
                                + safeDirectory
                );
            }

            String normalizedExtension =
                    extension == null
                            ? ""
                            : extension.trim().toLowerCase();

            try (var files = Files.list(safeDirectory)) {

                List<String> result = files
                        .filter(Files::isRegularFile)
                        .filter(path -> {

                            String fileName =
                                    path.getFileName()
                                            .toString()
                                            .toLowerCase();

                            boolean matches =
                                    normalizedExtension.isBlank()
                                            || fileName.endsWith(
                                            normalizedExtension);

                            log.info(
                                    "file={}, matches={}",
                                    fileName,
                                    matches
                            );

                            return matches;
                        })
                        .map(Path::toString)
                        .toList();

                log.info(
                        "listFiles result count={}",
                        result.size()
                );

                log.info(
                        "listFiles result={}",
                        result
                );

                log.info("========== listFiles END ==========");

                return result;
            }

        } catch (IllegalArgumentException e) {

            log.warn(
                    "Access denied while listing directory: {}",
                    e.getMessage()
            );

            return List.of(
                    "Access denied: " + e.getMessage()
            );

        } catch (IOException e) {

            log.error(
                    "Failed to list files, directory={}, extension={}",
                    directory,
                    extension,
                    e
            );

            return List.of(
                    "Error: " + e.getMessage()
            );
        }
    }


    // ============================================================
    // 3. WRITE FILE
    // ============================================================

    @McpTool(
            name = "writeFile",
            description = "Creates or overwrites a text file with the provided content",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public String writeFile(
            @McpToolParam(
                    required = true,
                    description = "Absolute path of the file to write"
            )
            String path,

            @McpToolParam(
                    required = true,
                    description = "Text content to write to the file"
            )
            String content) {

        log.info("writeFile called with path={}", path);

        try {

            Path safePath =
                    safeFilePath.validate(path);

            Files.writeString(
                    safePath,
                    content
            );

            log.info(
                    "File written successfully: {}",
                    safePath
            );

            return "File written successfully: "
                    + safePath;

        } catch (IllegalArgumentException e) {

            log.warn(
                    "Access denied while writing file: {}",
                    e.getMessage()
            );

            return "Access denied: " + e.getMessage();

        } catch (IOException e) {

            log.error(
                    "Failed to write file: {}",
                    path,
                    e
            );

            return "Could not write file: "
                    + e.getMessage();
        }
    }


    // ============================================================
    // 4. RENAME / MOVE FILE
    // ============================================================

    @McpTool(
            name = "renameFile",
            description = "Renames a file from one path to another",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = false,
                    openWorldHint = false
            )
    )
    public String renameFile(
            @McpToolParam(
                    required = true,
                    description = "Current absolute path of the file"
            )
            String sourcePath,

            @McpToolParam(
                    required = true,
                    description = "New absolute path of the file"
            )
            String targetPath) {

        log.info(
                "renameFile called, source={}, target={}",
                sourcePath,
                targetPath
        );

        try {

            Path safeSource =
                    safeFilePath.validate(sourcePath);

            Path safeTarget =
                    safeFilePath.validate(targetPath);

            if (!Files.exists(safeSource)) {
                return "Source file does not exist: "
                        + safeSource;
            }

            if (!Files.isRegularFile(safeSource)) {
                return "Source is not a regular file: "
                        + safeSource;
            }

            if (Files.exists(safeTarget)) {
                return "Target already exists: "
                        + safeTarget;
            }

            Files.move(
                    safeSource,
                    safeTarget
            );

            log.info(
                    "File renamed successfully, source={}, target={}",
                    safeSource,
                    safeTarget
            );

            return "File renamed successfully: "
                    + safeSource
                    + " -> "
                    + safeTarget;

        } catch (IllegalArgumentException e) {

            log.warn(
                    "Access denied during rename: {}",
                    e.getMessage()
            );

            return "Access denied: " + e.getMessage();

        } catch (IOException e) {

            log.error(
                    "Failed to rename file, source={}, target={}",
                    sourcePath,
                    targetPath,
                    e
            );

            return "Could not rename file: "
                    + e.getMessage();
        }
    }


    // ============================================================
    // 5. COPY FILE
    // ============================================================

    @McpTool(
            name = "copyFile",
            description = "Copies a file from one path to another",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = false
            )
    )
    public String copyFile(
            @McpToolParam(
                    required = true,
                    description = "Absolute path of the source file"
            )
            String sourcePath,

            @McpToolParam(
                    required = true,
                    description = "Absolute path of the destination file"
            )
            String targetPath) {

        log.info(
                "copyFile called, source={}, target={}",
                sourcePath,
                targetPath
        );

        try {

            Path safeSource =
                    safeFilePath.validate(sourcePath);

            Path safeTarget =
                    safeFilePath.validate(targetPath);

            if (!Files.exists(safeSource)) {
                return "Source file does not exist: "
                        + safeSource;
            }

            if (!Files.isRegularFile(safeSource)) {
                return "Source is not a regular file: "
                        + safeSource;
            }

            if (Files.exists(safeTarget)) {
                return "Target already exists: "
                        + safeTarget;
            }

            Files.copy(
                    safeSource,
                    safeTarget
            );

            log.info(
                    "File copied successfully, source={}, target={}",
                    safeSource,
                    safeTarget
            );

            return "File copied successfully: "
                    + safeSource
                    + " -> "
                    + safeTarget;

        } catch (IllegalArgumentException e) {

            log.warn(
                    "Access denied during copy: {}",
                    e.getMessage()
            );

            return "Access denied: " + e.getMessage();

        } catch (IOException e) {

            log.error(
                    "Failed to copy file, source={}, target={}",
                    sourcePath,
                    targetPath,
                    e
            );

            return "Could not copy file: "
                    + e.getMessage();
        }
    }


    // ============================================================
    // 6. DELETE FILE
    // ============================================================

    @McpTool(
            name = "deleteFile",
            description = "Deletes a file from the filesystem",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public String deleteFile(
            @McpToolParam(
                    required = true,
                    description = "Absolute path of the file to delete"
            )
            String path) {

        log.info(
                "deleteFile called with path={}",
                path
        );

        try {

            Path safePath =
                    safeFilePath.validate(path);

            if (!Files.exists(safePath)) {

                log.warn(
                        "File does not exist: {}",
                        safePath
                );

                return "File does not exist: "
                        + safePath;
            }

            if (!Files.isRegularFile(safePath)) {

                log.warn(
                        "Path is not a regular file: {}",
                        safePath
                );

                return "Path is not a regular file: "
                        + safePath;
            }

            Files.delete(safePath);

            log.info(
                    "File deleted successfully: {}",
                    safePath
            );

            return "File deleted successfully: "
                    + safePath;

        } catch (IllegalArgumentException e) {

            log.warn(
                    "Access denied while deleting file: {}",
                    e.getMessage()
            );

            return "Access denied: "
                    + e.getMessage();

        } catch (IOException e) {

            log.error(
                    "Failed to delete file: {}",
                    path,
                    e
            );

            return "Could not delete file: "
                    + e.getMessage();
        }
    }


    @McpTool(
            name = "showClientRoots",
            description = "Returns the filesystem roots provided by the MCP client",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public String showClientRoots(McpSyncRequestContext context) {

        if (!context.rootsEnabled()) {
            return "Client does not support MCP roots";
        }

        var roots = context.roots();

        log.info("Client roots: {}", roots);

        return roots.toString();
    }
}