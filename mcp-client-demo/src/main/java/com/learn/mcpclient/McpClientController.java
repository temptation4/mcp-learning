package com.learn.mcpclient;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpClientController {

    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    public McpClientController(SyncMcpToolCallbackProvider toolCallbackProvider) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @GetMapping("/tools")
    public List<Map<String, String>> tools() {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(ToolCallback::getToolDefinition)
                .map(definition -> Map.of(
                        "name", definition.name(),
                        "description", definition.description() == null
                                ? ""
                                : definition.description()
                ))
                .toList();
    }

    @PostMapping("/read")
    public String readFile(@RequestParam String path) {

        ToolCallback readFileTool =
                Arrays.stream(
                                toolCallbackProvider.getToolCallbacks()
                        )
                        .filter(callback ->
                                callback.getToolDefinition()
                                        .name()
                                        .equals("readFile"))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "readFile tool not found"));

        String arguments = """
                {
                  "path": "%s"
                }
                """.formatted(path);

        return readFileTool.call(arguments);
    }

    @PostMapping("/add-time")
    public String addTime(
            @RequestParam int a,
            @RequestParam int b) {

        ToolCallback tool = Arrays.stream(
                        toolCallbackProvider.getToolCallbacks())
                .filter(callback ->
                        callback.getToolDefinition()
                                .name()
                                .equals("add"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("add tool not found"));

        return tool.call("""
        {
          "a": %d,
          "b": %d
        }
        """.formatted(a, b));
    }

    @PostMapping("/add-file")
    public String addFile(
            @RequestParam int a,
            @RequestParam int b) {

        ToolCallback tool = Arrays.stream(
                        toolCallbackProvider.getToolCallbacks())
                .filter(callback ->
                        callback.getToolDefinition()
                                .name()
                                .equals("alt_1_add"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("alt_1_add tool not found"));

        String arguments = """
            {
              "a": %d,
              "b": %d
            }
            """.formatted(a, b);

        return tool.call(arguments);
    }

    @GetMapping("/roots")
    public String showRoots() {

        ToolCallback tool = Arrays.stream(
                        toolCallbackProvider.getToolCallbacks())
                .filter(callback ->
                        callback.getToolDefinition()
                                .name()
                                .equals("showClientRoots"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "showClientRoots tool not found"));

        return tool.call("{}");
    }


}
