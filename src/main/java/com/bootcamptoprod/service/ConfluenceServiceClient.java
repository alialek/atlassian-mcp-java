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
@EnableConfigurationProperties(AtlassianConfig.ConfluenceConfig.class)
public class ConfluenceServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ConfluenceServiceClient.class);
    private final RestTemplate restTemplate;

    public ConfluenceServiceClient(RestTemplateBuilder builder, AtlassianConfig.ConfluenceConfig config) {
        if (!config.isAuthConfigured()) {
            throw new IllegalStateException("Confluence configuration is not properly configured");
        }

        RestTemplateBuilder configuredBuilder = builder.rootUri(config.getUrl() + "/rest/api");
        
        if (config.getPersonalToken() != null) {
            configuredBuilder = configuredBuilder.defaultHeader("Authorization", "Bearer " + config.getPersonalToken());
        } else {
            configuredBuilder = configuredBuilder.basicAuthentication(config.getUsername(), config.getApiToken());
        }

        this.restTemplate = configuredBuilder.build();
        logger.info("ConfluenceServiceClient initialized for URL: {}", config.getUrl());
    }

    /**
     * List all spaces in Confluence.
     */
    @Tool(description = "Получить список всех пространств в Confluence", name = "listSpaces")
    public Map<String, Object> listSpaces() {
        logger.info("Fetching list of spaces from Confluence...");
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("/space", Map.class);
            logger.info("Successfully retrieved spaces.");
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error listing spaces: {}", e.getMessage(), e);
            return Map.of("error", "Error listing spaces: " + e.getMessage());
        }
    }

    /**
     * Search Confluence content using CQL.
     * @param cql CQL query string. Examples: 'type=page AND space=DEV', 'title~"Meeting Notes"', 'text~"keyword"', 'space="DEV" AND created>=2024-01-01'
     * @param limit Maximum number of results to return (1-100). Default: 10
     */
    @Tool(description = "Поиск контента в Confluence с использованием CQL (Confluence Query Language)", name = "searchContent")
    public Map<String, Object> searchContent(
            @ToolParam(description = "CQL запрос. Примеры: 'type=page AND space=DEV', 'title~\"Заметки\"', 'text~\"важный\"'") String cql,
            @ToolParam(description = "Максимальное количество результатов (1-100). По умолчанию: 10") Integer limit) {
        logger.info("Searching Confluence content with CQL: {}", cql);
        try {
            String url = "/content/search?cql={cql}&limit={limit}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, cql, limit != null ? limit : 10);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error searching content: {}", e.getMessage(), e);
            return Map.of("error", "Error searching content: " + e.getMessage());
        }
    }

    /**
     * Get page content by ID.
     * @param pageId Confluence page ID (numeric string, e.g., "123456")
     * @param includeBody Whether to include page body content (true/false). Default: false
     */
    @Tool(description = "Получить страницу Confluence по ID", name = "getPageById")
    public Map<String, Object> getPageById(
            @ToolParam(description = "ID страницы Confluence (числовая строка, например '123456')") String pageId, 
            @ToolParam(description = "Включить содержимое страницы (true/false). По умолчанию: false") Boolean includeBody) {
        logger.info("Fetching page by ID: {}", pageId);
        try {
            String expand = includeBody != null && includeBody ? "body.storage,version,space" : "version,space";
            String url = "/content/{pageId}?expand={expand}";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, pageId, expand);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching page {}: {}", pageId, e.getMessage(), e);
            return Map.of("error", "Error fetching page: " + e.getMessage());
        }
    }

    /**
     * Update an existing page.
     * @param pageId Page ID to update (numeric string, e.g., "123456")
     * @param title New page title
     * @param content New page content in Confluence storage format
     * @param currentVersion Current version number of the page (required for update)
     */
    @Tool(description = "Обновить существующую страницу Confluence", name = "updatePage")
    public Map<String, Object> updatePage(
            @ToolParam(description = "ID страницы для обновления (числовая строка, например '123456')") String pageId,
            @ToolParam(description = "Новый заголовок страницы") String title,
            @ToolParam(description = "Новое содержимое страницы в формате Confluence storage") String content,
            @ToolParam(description = "Текущий номер версии страницы (обязательно для обновления)") Integer currentVersion) {
        logger.info("Updating page: {}", pageId);
        try {
            Map<String, Object> updateData = Map.of(
                "id", pageId,
                "type", "page",
                "title", title,
                "body", Map.of("storage", Map.of("value", content, "representation", "storage")),
                "version", Map.of("number", currentVersion + 1)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateData, headers);

            ResponseEntity<Map> response = restTemplate.putForObject("/content/{pageId}", request, Map.class, pageId);
            return Map.of("success", true, "page", response);
        } catch (Exception e) {
            logger.error("Error updating page {}: {}", pageId, e.getMessage(), e);
            return Map.of("error", "Error updating page: " + e.getMessage());
        }
    }

    /**
     * Get the number of documents in a specific space.
     */
    @Tool(description = "Получить количество документов в определенном пространстве Confluence", name = "getDocumentCountInSpace")
    public Map<String, Object> getDocumentCountInSpace(
            @ToolParam(description = "Ключ пространства (например, 'DEV', 'TEAM')") String spaceKey) {
        logger.info("Fetching document count for space: {}", spaceKey);
        try {
            String cql = "space=" + spaceKey;
            ResponseEntity<Map> response = restTemplate.getForEntity("/content/search?cql={cql}", Map.class, cql);
            logger.info("Successfully retrieved document count for space: {}", spaceKey);
            return Map.of("documentCount", response.getBody().get("size"));
        } catch (Exception e) {
            logger.error("Error fetching document count for space {}: {}", spaceKey, e.getMessage(), e);
            return Map.of("error", "Error fetching document count: " + e.getMessage());
        }
    }

    /**
     * List documents in a specific space.
     */
    @Tool(description = "Получить список всех документов в определенном пространстве Confluence", name = "listDocumentsInSpace")
    public Map<String, Object> listDocumentsInSpace(
            @ToolParam(description = "Ключ пространства (например, 'DEV', 'TEAM')") String spaceKey) {
        logger.info("Fetching documents in space: {}", spaceKey);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("/space/{spaceKey}/content", Map.class, spaceKey);
            logger.info("Successfully retrieved documents in space: {}", spaceKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error listing documents in space {}: {}", spaceKey, e.getMessage(), e);
            return Map.of("error", "Error listing documents: " + e.getMessage());
        }
    }

    /**
     * Create a new document with specified content.
     * @param spaceKey Space key (e.g., "DEV", "TEAM", "DOCS")
     * @param title Page title (e.g., "Meeting Notes", "API Documentation")
     * @param content Page content in Confluence storage format (HTML-like)
     */
    @Tool(description = "Создать новый документ Confluence с заданным заголовком и содержимым", name = "createDocument")
    public Map<String, Object> createDocument(
            @ToolParam(description = "Ключ пространства (например, 'DEV', 'TEAM', 'DOCS')") String spaceKey,
            @ToolParam(description = "Заголовок страницы (например, 'Заметки встречи', 'API документация')") String title,
            @ToolParam(description = "Содержимое страницы в формате Confluence storage (HTML-подобный)") String content) {
        logger.info("Creating a new document '{}' in space '{}'", title, spaceKey);
        try {
            String payload = String.format(
                    "{\"type\":\"page\",\"title\":\"%s\",\"space\":{\"key\":\"%s\"},\"body\":{\"storage\":{\"value\":\"%s\",\"representation\":\"storage\"}}}",
                    title, spaceKey, content
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("/content", request, Map.class);
            logger.info("Successfully created document '{}' in space '{}'", title, spaceKey);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating document '{}' in space '{}': {}", title, spaceKey, e.getMessage(), e);
            return Map.of("error", "Error creating document: " + e.getMessage());
        }
    }

    /**
     * Extract page history of a specific document.
     */
    @Tool(description = "Получить историю версий определенного документа Confluence", name = "getPageHistory")
    public Map<String, Object> getPageHistory(
            @ToolParam(description = "ID документа (числовая строка, например '123456')") String documentId) {
        logger.info("Fetching page history for document: {}", documentId);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("/content/{id}/version", Map.class, documentId);
            logger.info("Successfully retrieved page history for document: {}", documentId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching page history for document {}: {}", documentId, e.getMessage(), e);
            return Map.of("error", "Error fetching page history: " + e.getMessage());
        }
    }

    /**
     * Extract metadata of a specific document.
     */
    @Tool(description = "Получить метаданные определенного документа Confluence", name = "getDocumentMetadata")
    public Map<String, Object> getDocumentMetadata(
            @ToolParam(description = "ID документа (числовая строка, например '123456')") String documentId) {
        logger.info("Fetching metadata for document: {}", documentId);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("/content/{id}", Map.class, documentId);
            logger.info("Successfully retrieved metadata for document: {}", documentId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching metadata for document {}: {}", documentId, e.getMessage(), e);
            return Map.of("error", "Error fetching document metadata: " + e.getMessage());
        }
    }
}