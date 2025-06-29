package com.bootcamptoprod.config;

import com.bootcamptoprod.service.ConfluenceServiceClient;
import com.bootcamptoprod.service.JiraServiceClient;
import com.bootcamptoprod.service.ZephyrServiceClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties({
    AtlassianConfig.ConfluenceConfig.class,
    AtlassianConfig.JiraConfig.class,
    AtlassianConfig.ZephyrConfig.class
})
public class McpConfiguration {

    @Bean
    public ToolCallbackProvider atlassianTools(
            @ConditionalOnProperty(name = "confluence.url") ConfluenceServiceClient confluenceServiceClient,
            @ConditionalOnProperty(name = "jira.url") JiraServiceClient jiraServiceClient,
            @ConditionalOnProperty(name = "zephyr.base-url") ZephyrServiceClient zephyrServiceClient) {
        
        List<Object> services = new ArrayList<>();
        
        if (confluenceServiceClient != null) {
            services.add(confluenceServiceClient);
        }
        if (jiraServiceClient != null) {
            services.add(jiraServiceClient);
        }
        if (zephyrServiceClient != null) {
            services.add(zephyrServiceClient);
        }
        
        return MethodToolCallbackProvider.builder()
                .toolObjects(services.toArray())
                .build();
    }
}
