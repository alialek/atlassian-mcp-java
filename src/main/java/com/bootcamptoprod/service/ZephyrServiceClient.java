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

import java.time.Duration;
import java.util.Map;

@Service
@EnableConfigurationProperties(AtlassianConfig.ZephyrConfig.class)
public class ZephyrServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ZephyrServiceClient.class);
    private final RestTemplate restTemplate;

    public ZephyrServiceClient(RestTemplateBuilder builder, AtlassianConfig.ZephyrConfig config) {
        if (!config.isAuthConfigured()) {
            throw new IllegalStateException("Zephyr configuration is not properly configured");
        }

        this.restTemplate = builder
                .rootUri(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getBearerToken())
                .setConnectTimeout(Duration.ofSeconds(config.getTimeout()))
                .setReadTimeout(Duration.ofSeconds(config.getTimeout()))
                .build();
        
        logger.info("ZephyrServiceClient initialized for URL: {}", config.getBaseUrl());
    }

    @Tool(description = "Get test case details from Zephyr")
    public Map<String, Object> getTestCase(String testCaseKey) {
        logger.info("Fetching test case: {}", testCaseKey);
        try {
            String url = "/rest/atm/1.0/testcase/{testCaseKey}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, testCaseKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching test case {}: {}", testCaseKey, e.getMessage(), e);
            return Map.of("error", "Error fetching test case: " + e.getMessage());
        }
    }

    @Tool(description = "Search test cases in Zephyr using TQL")
    public Map<String, Object> searchTestCases(String tqlQuery, Integer maxResults) {
        logger.info("Searching test cases with TQL: {}", tqlQuery);
        try {
            String url = "/rest/atm/1.0/testcase/search?tql={tql}&maxResults={maxResults}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, 
                tqlQuery, maxResults != null ? maxResults : 50);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error searching test cases: {}", e.getMessage(), e);
            return Map.of("error", "Error searching test cases: " + e.getMessage());
        }
    }

    @Tool(description = "Create a new test case in Zephyr")
    public Map<String, Object> createTestCase(String projectKey, String name, String status) {
        logger.info("Creating test case in project: {}", projectKey);
        try {
            Map<String, Object> testCaseData = Map.of(
                "projectKey", projectKey,
                "name", name,
                "status", status != null ? status : "Draft"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(testCaseData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("/rest/atm/1.0/testcase", request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating test case: {}", e.getMessage(), e);
            return Map.of("error", "Error creating test case: " + e.getMessage());
        }
    }

    @Tool(description = "Get test plan details from Zephyr")
    public Map<String, Object> getTestPlan(String testPlanKey) {
        logger.info("Fetching test plan: {}", testPlanKey);
        try {
            String url = "/rest/atm/1.0/testplan/{testPlanKey}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, testPlanKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching test plan {}: {}", testPlanKey, e.getMessage(), e);
            return Map.of("error", "Error fetching test plan: " + e.getMessage());
        }
    }

    @Tool(description = "Create a new test plan in Zephyr")
    public Map<String, Object> createTestPlan(String projectKey, String name, String objective) {
        logger.info("Creating test plan in project: {}", projectKey);
        try {
            Map<String, Object> testPlanData = Map.of(
                "projectKey", projectKey,
                "name", name,
                "objective", objective != null ? objective : ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(testPlanData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("/rest/atm/1.0/testplan", request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating test plan: {}", e.getMessage(), e);
            return Map.of("error", "Error creating test plan: " + e.getMessage());
        }
    }

    @Tool(description = "Get test run details from Zephyr")
    public Map<String, Object> getTestRun(String testRunKey) {
        logger.info("Fetching test run: {}", testRunKey);
        try {
            String url = "/rest/atm/1.0/testrun/{testRunKey}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, testRunKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching test run {}: {}", testRunKey, e.getMessage(), e);
            return Map.of("error", "Error fetching test run: " + e.getMessage());
        }
    }

    @Tool(description = "Create test execution result in Zephyr")
    public Map<String, Object> createTestResult(String testCaseKey, String status, String comment) {
        logger.info("Creating test result for test case: {}", testCaseKey);
        try {
            Map<String, Object> resultData = Map.of(
                "testCaseKey", testCaseKey,
                "status", status,
                "comment", comment != null ? comment : ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(resultData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("/rest/atm/1.0/testresult", request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating test result: {}", e.getMessage(), e);
            return Map.of("error", "Error creating test result: " + e.getMessage());
        }
    }

    @Tool(description = "Get test steps for a test case")
    public Map<String, Object> getTestSteps(String issueId, String projectId) {
        logger.info("Fetching test steps for issue: {}", issueId);
        try {
            String url = "/rest/atm/1.0/testcase/{issueId}/teststeps?projectId={projectId}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, issueId, projectId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching test steps: {}", e.getMessage(), e);
            return Map.of("error", "Error fetching test steps: " + e.getMessage());
        }
    }

    @Tool(description = "Add test step to a test case")
    public Map<String, Object> addTestStep(String issueId, String projectId, String step, String data, String result) {
        logger.info("Adding test step to issue: {}", issueId);
        try {
            Map<String, Object> stepData = Map.of(
                "step", step,
                "data", data != null ? data : "",
                "result", result != null ? result : ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(stepData, headers);

            String url = "/rest/atm/1.0/testcase/{issueId}/teststep?projectId={projectId}";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class, issueId, projectId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error adding test step: {}", e.getMessage(), e);
            return Map.of("error", "Error adding test step: " + e.getMessage());
        }
    }

    @Tool(description = "Get environments for a project")
    public Map<String, Object> getEnvironments(String projectKey) {
        logger.info("Fetching environments for project: {}", projectKey);
        try {
            String url = "/rest/atm/1.0/environments?projectKey={projectKey}";
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class, projectKey);
            return Map.of("environments", response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching environments: {}", e.getMessage(), e);
            return Map.of("error", "Error fetching environments: " + e.getMessage());
        }
    }
} 