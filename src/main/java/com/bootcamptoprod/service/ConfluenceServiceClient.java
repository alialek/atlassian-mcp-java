package com.bootcamptoprod.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ConfluenceServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ConfluenceServiceClient.class);

    private final RestTemplate restTemplate;

    public ConfluenceServiceClient(RestTemplateBuilder builder,
                                   @Value("${confluence.auth.email}") String email,
                                   @Value("${confluence.auth.apiToken}") String apiToken,
                                   @Value("${confluence.baseUrl}") String confluenceBaseUrl) {
        this.restTemplate = builder
                .rootUri(confluenceBaseUrl)
                .basicAuthentication(email, apiToken)
                .build();
        logger.info("ConfluenceServiceClient initialized");
    }

    /**
     * List all spaces in Confluence.
     */
    @Tool(description = "List all spaces in Confluence.")
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
     * Get the number of documents in a specific space.
     */
    @Tool(description = "Get the number of documents in a specific Confluence space.")
    public Map<String, Object> getDocumentCountInSpace(String spaceKey) {
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
    @Tool(description = "List all documents in a specific Confluence space.")
    public Map<String, Object> listDocumentsInSpace(String spaceKey) {
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
     */
    @Tool(description = "Create a new Confluence document with a given title and content in a specific space.")
    public Map<String, Object> createDocument(String spaceKey, String title, String content) {
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
    @Tool(description = "Extract the version history of a specific Confluence document.")
    public Map<String, Object> getPageHistory(String documentId) {
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
    @Tool(description = "Extract metadata of a specific Confluence document.")
    public Map<String, Object> getDocumentMetadata(String documentId) {
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