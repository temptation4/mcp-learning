package com.learn.mcpclient;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/delete")
public class DeleteController {

    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    private final Map<String, String> pendingDeletes =
            new ConcurrentHashMap<>();

    public DeleteController(SyncMcpToolCallbackProvider toolCallbackProvider) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @PostMapping("/request")
    public String requestDelete(@RequestParam String path) {

        String confirmationId = UUID.randomUUID().toString();

        pendingDeletes.put(confirmationId, path);

        return """
                Delete requested.

                File: %s

                Confirmation ID: %s

                Call POST /delete/confirm with this confirmation ID
                to actually delete the file.
                """.formatted(path, confirmationId);
    }

    @PostMapping("/confirm")
    public String confirmDelete(@RequestParam String confirmationId) {

        String path = pendingDeletes.remove(confirmationId);

        if (path == null) {
            return "Invalid or already-used confirmation ID.";
        }

        ToolCallback deleteTool =
                java.util.Arrays.stream(
                                toolCallbackProvider.getToolCallbacks())
                        .filter(callback ->
                                callback.getToolDefinition()
                                        .name()
                                        .equals("deleteFile"))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "deleteFile MCP tool not found"));

        String arguments = """
                {
                  "path": "%s"
                }
                """.formatted(path);

        return deleteTool.call(arguments);
    }
}