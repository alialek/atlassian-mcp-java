package com.bootcamptoprod.service;

import com.bootcamptoprod.config.AtlassianConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@EnableConfigurationProperties(AtlassianConfig.JiraConfig.class)
public class JiraServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(JiraServiceClient.class);
    private final RestTemplate restTemplate;

    public JiraServiceClient(RestTemplateBuilder builder, AtlassianConfig.JiraConfig config) {
        if (!config.isAuthConfigured()) {
            throw new IllegalStateException("Jira configuration is not properly configured");
        }

        RestTemplateBuilder configuredBuilder = builder.rootUri(config.getUrl() + "/rest/api/3");
        
        if (config.getPersonalToken() != null) {
            configuredBuilder = configuredBuilder.defaultHeader("Authorization", "Bearer " + config.getPersonalToken());
        } else {
            configuredBuilder = configuredBuilder.basicAuthentication(config.getUsername(), config.getApiToken());
        }

        this.restTemplate = configuredBuilder.build();
        logger.info("JiraServiceClient initialized for URL: {}", config.getUrl());
    }

    /**
     * Search Jira issues using JQL.
     * @param jql JQL query string. Examples: 'project = "DEV"', 'assignee = currentUser()', 'status = "In Progress"'
     * @param maxResults Maximum number of results to return (1-100). Default: 50
     */
    @Tool(description = "Поиск задач в Jira с использованием JQL (Jira Query Language)", name = "searchIssues")
    public Map<String, Object> searchIssues(
            @ToolParam(description = "JQL запрос. Примеры: 'project = \"DEV\"', 'assignee = currentUser()', 'status = \"В работе\"'") String jql,
            @ToolParam(description = "Максимальное количество результатов (1-100). По умолчанию: 50") Integer maxResults) {
        logger.info("Searching Jira issues with JQL: {}", jql);
        try {
            String url = "/search?jql={jql}&maxResults={maxResults}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, jql, maxResults != null ? maxResults : 50);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error searching issues: {}", e.getMessage(), e);
            return Map.of("error", "Error searching issues: " + e.getMessage());
        }
    }

    /**
     * Get details of a specific Jira issue.
     * @param issueKey Jira issue key (e.g., "DEV-123", "PROJ-456")
     */
    @Tool(description = "Получить детали конкретной задачи Jira", name = "getIssue")
    public Map<String, Object> getIssue(
            @ToolParam(description = "Ключ задачи Jira (например, 'DEV-123', 'PROJ-456')") String issueKey) {
        logger.info("Fetching Jira issue: {}", issueKey);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("/issue/{issueKey}", Map.class, issueKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching issue {}: {}", issueKey, e.getMessage(), e);
            return Map.of("error", "Error fetching issue: " + e.getMessage());
        }
    }

    /**
     * Create a new Jira issue.
     * @param projectKey Project key (e.g., "DEV", "PROJ", "SUPPORT")
     * @param summary Issue summary/title (e.g., "Fix login bug", "Add new feature")
     * @param issueType Issue type (e.g., "Task", "Bug", "Story", "Epic")
     * @param description Issue description (optional, plain text)
     */
    @Tool(description = "Создать новую задачу в Jira", name = "createIssue")
    public Map<String, Object> createIssue(
            @ToolParam(description = "Ключ проекта (например, 'DEV', 'PROJ', 'SUPPORT')") String projectKey,
            @ToolParam(description = "Краткое описание/заголовок задачи (например, 'Исправить ошибку входа')") String summary,
            @ToolParam(description = "Тип задачи (например, 'Task', 'Bug', 'Story', 'Epic')") String issueType,
            @ToolParam(description = "Описание задачи (необязательно, простой текст)") String description) {
        logger.info("Creating Jira issue in project: {}", projectKey);
        try {
            Map<String, Object> issueData = Map.of(
                "fields", Map.of(
                    "project", Map.of("key", projectKey),
                    "summary", summary,
                    "issuetype", Map.of("name", issueType),
                    "description", Map.of(
                        "type", "doc",
                        "version", 1,
                        "content", new Object[]{
                            Map.of(
                                "type", "paragraph",
                                "content", new Object[]{
                                    Map.of("type", "text", "text", description != null ? description : "")
                                }
                            )
                        }
                    )
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(issueData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("/issue", request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating issue: {}", e.getMessage(), e);
            return Map.of("error", "Error creating issue: " + e.getMessage());
        }
    }

    /**
     * Update a Jira issue.
     * @param issueKey Issue key to update (e.g., "DEV-123")
     * @param summary New issue summary/title
     * @param description New issue description (plain text)
     */
    @Tool(description = "Обновить задачу Jira", name = "updateIssue")
    public Map<String, Object> updateIssue(
            @ToolParam(description = "Ключ задачи для обновления (например, 'DEV-123')") String issueKey,
            @ToolParam(description = "Новое краткое описание/заголовок задачи") String summary,
            @ToolParam(description = "Новое описание задачи (простой текст)") String description) {
        logger.info("Updating Jira issue: {}", issueKey);
        try {
            Map<String, Object> updateData = Map.of(
                "fields", Map.of(
                    "summary", summary,
                    "description", Map.of(
                        "type", "doc",
                        "version", 1,
                        "content", new Object[]{
                            Map.of(
                                "type", "paragraph",
                                "content", new Object[]{
                                    Map.of("type", "text", "text", description != null ? description : "")
                                }
                            )
                        }
                    )
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateData, headers);

            restTemplate.put("/issue/{issueKey}", request, issueKey);
            return Map.of("success", true, "message", "Issue updated successfully");
        } catch (Exception e) {
            logger.error("Error updating issue {}: {}", issueKey, e.getMessage(), e);
            return Map.of("error", "Error updating issue: " + e.getMessage());
        }
    }

    @Tool(description = "Получить все доступные пользователю проекты", name = "getProjects")
    public Map<String, Object> getProjects() {
        logger.info("Fetching Jira projects");
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity("/project", Object[].class);
            return Map.of("projects", response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching projects: {}", e.getMessage(), e);
            return Map.of("error", "Error fetching projects: " + e.getMessage());
        }
    }

    @Tool(description = "Получить версии проекта", name = "getProjectVersions")
    public Map<String, Object> getProjectVersions(
            @ToolParam(description = "Ключ проекта (например, 'DEV', 'PROJ')") String projectKey) {
        logger.info("Fetching versions for project: {}", projectKey);
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity("/project/{projectKey}/version", Object[].class, projectKey);
            return Map.of("versions", response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching project versions: {}", e.getMessage(), e);
            return Map.of("error", "Error fetching project versions: " + e.getMessage());
        }
    }

    @Tool(description = "Добавить комментарий к задаче Jira", name = "addComment")
    public Map<String, Object> addComment(
            @ToolParam(description = "Ключ задачи (например, 'DEV-123')") String issueKey,
            @ToolParam(description = "Текст комментария") String comment) {
        logger.info("Adding comment to issue: {}", issueKey);
        try {
            Map<String, Object> commentData = Map.of(
                "body", Map.of(
                    "type", "doc",
                    "version", 1,
                    "content", new Object[]{
                        Map.of(
                            "type", "paragraph",
                            "content", new Object[]{
                                Map.of("type", "text", "text", comment)
                            }
                        )
                    }
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(commentData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("/issue/{issueKey}/comment", request, Map.class, issueKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error adding comment to issue {}: {}", issueKey, e.getMessage(), e);
            return Map.of("error", "Error adding comment: " + e.getMessage());
        }
    }
} 