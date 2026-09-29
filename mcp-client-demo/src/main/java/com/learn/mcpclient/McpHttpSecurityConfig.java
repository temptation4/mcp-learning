/*
package com.learn.mcpclient;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class McpHttpSecurityConfig {

    @Bean
    McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>
    timeServerBasicAuth() {

        return (serverName, builder) -> {

            if (!"time-server".equals(serverName)) {
                return;
            }

            String username = "mcpclient";
            String password = "secret";

            String credentials = username + ":" + password;

            String basicAuth = Base64.getEncoder()
                    .encodeToString(
                            credentials.getBytes(StandardCharsets.UTF_8));

            builder.httpRequestCustomizer(
                    (requestBuilder, method, endpoint, body, context) -> {

                        requestBuilder.setHeader(
                                "Authorization",
                                "Basic " + basicAuth
                        );
                    }
            );
        };
    }
}*/
