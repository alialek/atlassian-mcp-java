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

    /**
     * Get test case details from Zephyr.
     * @param testCaseKey Test case key (e.g., "JQA-T123", "PROJ-T456")
     */
    @Tool(description = "Получить детали тест-кейса из Zephyr", name = "getTestCase")
    public Map<String, Object> getTestCase(
            @ToolParam(description = "Ключ тест-кейса (например, 'JQA-T123', 'PROJ-T456')") String testCaseKey) {
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

    /**
     * Search test cases in Zephyr using TQL.
     * @param tqlQuery TQL query string. Examples: 'projectKey = "JQA"', 'status = "Draft"', 'name ~ "login"'
     * @param maxResults Maximum number of results to return (1-100). Default: 50
     */
    @Tool(description = "Поиск тест-кейсов в Zephyr с использованием TQL (Test Query Language)", name = "searchTestCases")
    public Map<String, Object> searchTestCases(
            @ToolParam(description = "TQL запрос. Примеры: 'projectKey = \"JQA\"', 'status = \"Draft\"', 'name ~ \"логин\"'") String tqlQuery,
            @ToolParam(description = "Максимальное количество результатов (1-100). По умолчанию: 50") Integer maxResults) {
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

    /**
     * Create a new test case in Zephyr.
     * @param projectKey Project key (e.g., "JQA", "DEV", "TEST")
     * @param name Test case name (e.g., "Test user login", "Verify API response")
     * @param status Test case status (e.g., "Draft", "Approved", "Deprecated"). Default: "Draft"
     */
    @Tool(description = "Создать новый тест-кейс в Zephyr", name = "createTestCase")
    public Map<String, Object> createTestCase(
            @ToolParam(description = "Ключ проекта (например, 'JQA', 'DEV', 'TEST')") String projectKey,
            @ToolParam(description = "Название тест-кейса (например, 'Тест входа пользователя')") String name,
            @ToolParam(description = "Статус тест-кейса ('Draft', 'Approved', 'Deprecated'). По умолчанию: 'Draft'") String status) {
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

    @Tool(description = "Получить детали тест-плана из Zephyr", name = "getTestPlan")
    public Map<String, Object> getTestPlan(
            @ToolParam(description = "Ключ тест-плана (например, 'JQA-P123')") String testPlanKey) {
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

    @Tool(description = "Создать новый тест-план в Zephyr", name = "createTestPlan")
    public Map<String, Object> createTestPlan(
            @ToolParam(description = "Ключ проекта (например, 'JQA', 'DEV')") String projectKey,
            @ToolParam(description = "Название тест-плана") String name,
            @ToolParam(description = "Цель тестирования (необязательно)") String objective) {
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

    @Tool(description = "Получить детали тест-рана из Zephyr", name = "getTestRun")
    public Map<String, Object> getTestRun(
            @ToolParam(description = "Ключ тест-рана (например, 'JQA-R123')") String testRunKey) {
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

    /**
     * Create test execution result in Zephyr.
     * @param testCaseKey Test case key (e.g., "JQA-T123")
     * @param status Test execution status (e.g., "Pass", "Fail", "Blocked", "Not Executed")
     * @param comment Test execution comment/notes (optional)
     */
    @Tool(description = "Создать результат выполнения теста в Zephyr", name = "createTestResult")
    public Map<String, Object> createTestResult(
            @ToolParam(description = "Ключ тест-кейса (например, 'JQA-T123')") String testCaseKey,
            @ToolParam(description = "Статус выполнения ('Pass', 'Fail', 'Blocked', 'Not Executed')") String status,
            @ToolParam(description = "Комментарий/заметки к выполнению (необязательно)") String comment) {
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

    @Tool(description = "Получить шаги тестирования для тест-кейса", name = "getTestSteps")
    public Map<String, Object> getTestSteps(
            @ToolParam(description = "ID задачи JIRA (числовая строка, например '12345')") String issueId,
            @ToolParam(description = "ID проекта JIRA (числовая строка, например '10000')") String projectId) {
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

    /**
     * Add test step to a test case.
     * @param issueId JIRA issue ID (numeric string, e.g., "12345")
     * @param projectId JIRA project ID (numeric string, e.g., "10000") 
     * @param step Test step description (e.g., "Navigate to login page", "Enter credentials")
     * @param data Test data/input for this step (optional, e.g., "username: admin, password: test123")
     * @param result Expected result for this step (optional, e.g., "User should be logged in")
     */
    @Tool(description = "Добавить шаг тестирования к тест-кейсу", name = "addTestStep")
    public Map<String, Object> addTestStep(
            @ToolParam(description = "ID задачи JIRA (числовая строка, например '12345')") String issueId,
            @ToolParam(description = "ID проекта JIRA (числовая строка, например '10000')") String projectId,
            @ToolParam(description = "Описание шага тестирования (например, 'Перейти на страницу входа')") String step,
            @ToolParam(description = "Тестовые данные/входные данные для этого шага (необязательно)") String data,
            @ToolParam(description = "Ожидаемый результат для этого шага (необязательно)") String result) {
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

    @Tool(description = "Получить окружения для проекта", name = "getEnvironments")
    public Map<String, Object> getEnvironments(
            @ToolParam(description = "Ключ проекта (например, 'JQA', 'DEV')") String projectKey) {
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