package com.learn.mcpdb.config;

import com.learn.mcpdb.tool.DatabaseTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider databaseToolCallbacks(
            DatabaseTools databaseTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(databaseTools)
                .build();
    }
}