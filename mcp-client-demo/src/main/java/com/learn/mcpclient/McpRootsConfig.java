package com.learn.mcpclient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.stereotype.Component;

@Component
public class McpRootsConfig
        implements McpClientCustomizer<McpClient.SyncSpec> {

    @Override
    public void customize(
            String serverConfigurationName,
            McpClient.SyncSpec spec) {

        if ("file-server".equals(serverConfigurationName)) {

            spec.capabilities(
                    McpSchema.ClientCapabilities.builder()
                            .roots(true)
                            .build()
            );

            spec.roots(
                    new McpSchema.Root(
                            "file:///Users/neelu/Desktop/mcp-demo",
                            "MCP Demo Directory"
                    )
            );
        }
    }
}