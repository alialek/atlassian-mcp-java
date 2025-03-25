package com.bootcamptoprod.config;

import com.bootcamptoprod.service.ConfluenceServiceClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {

    @Bean
    public ToolCallbackProvider confluenceTools(ConfluenceServiceClient confluenceServiceClient) {
        return MethodToolCallbackProvider.builder().toolObjects(confluenceServiceClient).build();
    }
}
