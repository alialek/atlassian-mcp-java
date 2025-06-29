package com.bootcamptoprod.service;

import com.bootcamptoprod.config.AtlassianConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
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
    @Tool(description = "Search Jira issues using JQL (Jira Query Language). " +
                       "Parameters: jql (required) - JQL query like 'project = \"DEV\"' or 'assignee = currentUser()'; " +
                       "maxResults (optional) - Max results 1-100, default 50. " +
                       "Examples: 'project = \"DEV\" AND status = \"Open\"', 'assignee = currentUser() AND updated >= -7d'")
    public Map<String, Object> searchIssues(String jql, Integer maxResults) {
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
    @Tool(description = "Get details of a specific Jira issue. " +
                       "Parameters: issueKey (required) - Issue key like 'DEV-123' or 'PROJ-456'")
    public Map<String, Object> getIssue(String issueKey) {
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
    @Tool(description = "Create a new Jira issue. " +
                       "Parameters: projectKey (required) - Project key like 'DEV'; " +
                       "summary (required) - Issue title/summary; " +
                       "issueType (required) - Type like 'Task', 'Bug', 'Story'; " +
                       "description (optional) - Issue description text")
    public Map<String, Object> createIssue(String projectKey, String summary, String issueType, String description) {
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
    @Tool(description = "Update a Jira issue. " +
                       "Parameters: issueKey (required) - Issue key like 'DEV-123'; " +
                       "summary (required) - New summary/title; " +
                       "description (required) - New description text")
    public Map<String, Object> updateIssue(String issueKey, String summary, String description) {
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

    @Tool(description = "Get all projects accessible to the user")
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

    @Tool(description = "Get project versions")
    public Map<String, Object> getProjectVersions(String projectKey) {
        logger.info("Fetching versions for project: {}", projectKey);
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity("/project/{projectKey}/version", Object[].class, projectKey);
            return Map.of("versions", response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching project versions: {}", e.getMessage(), e);
            return Map.of("error", "Error fetching project versions: " + e.getMessage());
        }
    }

    @Tool(description = "Add comment to a Jira issue")
    public Map<String, Object> addComment(String issueKey, String comment) {
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