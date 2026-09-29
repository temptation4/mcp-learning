package com.learn.mcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ChatConfig {

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            SyncMcpToolCallbackProvider mcpTools) {

        ToolCallback[] safeTools = Arrays.stream(
                        mcpTools.getToolCallbacks())
                .filter(tool ->
                        !"deleteFile".equals(
                                tool.getToolDefinition().name()))
                .toArray(ToolCallback[]::new);

        return builder
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .temperature(0.1))
                .defaultTools(safeTools)
                .build();
    }
}