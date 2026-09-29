package com.learn.mcpclient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer
        .McpSyncHttpClientRequestCustomizer;

import org.springframework.ai.mcp.customizer.McpClientCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.client
        .AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client
        .OAuth2AuthorizedClientService;

import org.springaicommunity.mcp.security.client.sync
        .AuthenticationMcpTransportContextProvider;

import org.springaicommunity.mcp.security.client.sync.oauth2.http.client
        .OAuth2ClientCredentialsSyncHttpRequestCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class McpOAuthSecurityConfig {

    @Bean
    McpClientCustomizer<McpClient.SyncSpec> syncClientCustomizer() {

        return (name, syncSpec) ->
                syncSpec.transportContextProvider(
                        new AuthenticationMcpTransportContextProvider()
                );
    }

    @Bean
    AuthorizedClientServiceOAuth2AuthorizedClientManager
    authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );
    }

    // Wired directly onto the "time-server" connection's HTTP transport
    // builder, rather than exposed as a bare McpSyncHttpClientRequestCustomizer
    // bean for Spring AI's autoconfiguration to auto-detect: that mechanism
    // never actually invoked it in practice (confirmed - its own "Requesting
    // access token" / "Obtained access token" debug logs never appeared, so
    // every request went out with no Authorization header at all, hence the
    // 401). This uses the same direct builder.httpRequestCustomizer(...) call
    // the old Basic Auth version used, which is a proven working path, and
    // filters by server name so it only ever applies to "time-server".
    @Bean
    McpClientCustomizer<HttpClientStreamableHttpTransport.Builder> oauth2HttpCustomizer(
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {

        McpSyncHttpClientRequestCustomizer oauth2RequestCustomizer =
                new OAuth2ClientCredentialsSyncHttpRequestCustomizer(
                        authorizedClientManager,
                        "authserver-client-credentials"
                );

        return (serverName, builder) -> {
            if (!"time-server".equals(serverName)) {
                return;
            }
            builder.httpRequestCustomizer(oauth2RequestCustomizer);
        };
    }
}