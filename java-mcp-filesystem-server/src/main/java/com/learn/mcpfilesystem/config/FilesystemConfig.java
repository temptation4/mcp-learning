package com.learn.mcpfilesystem.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FilesystemProperties.class)
public class FilesystemConfig {
}
