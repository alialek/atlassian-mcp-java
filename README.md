# Atlassian MCP Server

This repository contains a comprehensive Atlassian MCP Server built with Spring Boot AI. The server integrates with **Confluence**, **Jira**, and **Zephyr** and exposes various operations as callable tools using the **@Tool** annotation.

For a detailed explanation on how to build an MCP server, check out our blog:  
**[Build MCP Servers with Spring Boot AI: A Beginner's Guide](https://bootcamptoprod.com/build-mcp-servers-with-spring-boot-ai/)**

---

## Features

- **Multi-Service Integration:** Connects to Confluence, Jira, and Zephyr
- **Flexible Authentication:** Supports Basic Auth, Personal Access Tokens, and Bearer tokens
- **Comprehensive Tools:** 40+ tools across document management, issue tracking, and test management
- **Read-Only Mode:** Optional read-only mode for safe operations
- **Conditional Service Loading:** Services are loaded only when properly configured

### Confluence Tools (8 tools)
- **searchContent** - Search using CQL queries (`cql`, `limit`)
- **listSpaces** - List all Confluence spaces (no parameters)
- **getPageById** - Get page content (`pageId`, `includeBody`)
- **createDocument** - Create new page (`spaceKey`, `title`, `content`)
- **updatePage** - Update existing page (`pageId`, `title`, `content`, `currentVersion`)
- **getDocumentCountInSpace** - Count documents in space (`spaceKey`)
- **listDocumentsInSpace** - List documents in space (`spaceKey`)
- **getPageHistory** - Get page version history (`documentId`)
- **getDocumentMetadata** - Get page metadata (`documentId`)

### Jira Tools (7 tools)
- **searchIssues** - Search using JQL queries (`jql`, `maxResults`)
- **getIssue** - Get issue details (`issueKey`)
- **createIssue** - Create new issue (`projectKey`, `summary`, `issueType`, `description`)
- **updateIssue** - Update existing issue (`issueKey`, `summary`, `description`)
- **getProjects** - List all projects (no parameters)
- **getProjectVersions** - Get project versions (`projectKey`)
- **addComment** - Add comment to issue (`issueKey`, `comment`)

### Zephyr Tools (9 tools)
- **getTestCase** - Get test case details (`testCaseKey`)
- **searchTestCases** - Search using TQL queries (`tqlQuery`, `maxResults`)
- **createTestCase** - Create new test case (`projectKey`, `name`, `status`)
- **getTestPlan** - Get test plan details (`testPlanKey`)
- **createTestPlan** - Create new test plan (`projectKey`, `name`, `objective`)
- **getTestRun** - Get test run details (`testRunKey`)
- **createTestResult** - Record test execution (`testCaseKey`, `status`, `comment`)
- **getTestSteps** - Get test steps (`issueId`, `projectId`)
- **addTestStep** - Add test step (`issueId`, `projectId`, `step`, `data`, `result`)
- **getEnvironments** - Get project environments (`projectKey`)

---

## Configuration

Set environment variables for the services you want to use:

### Confluence
```bash
CONFLUENCE_URL=https://your-domain.atlassian.net
CONFLUENCE_USERNAME=your-email@example.com
CONFLUENCE_API_TOKEN=your-api-token
```

### Jira
```bash
JIRA_URL=https://your-domain.atlassian.net  
JIRA_USERNAME=your-email@example.com
JIRA_API_TOKEN=your-api-token
```

### Zephyr
```bash
ZEPHYR_BASE_URL=https://your-domain.atlassian.net
ZEPHYR_BEARER_TOKEN=your-bearer-token
```

### Optional Settings
```bash
MCP_READ_ONLY=true  # Enable read-only mode
```

---

## Query Examples

### Confluence CQL Examples
```bash
# Search for pages in a specific space
type=page AND space=DEV

# Search by title
title~"Meeting Notes"

# Search by content
text~"API documentation"

# Search with date filter
text~"important" AND created>=2024-01-01

# Search in multiple spaces
space in (DEV, TEAM) AND type=page
```

### Jira JQL Examples
```bash
# Issues in specific project
project = "DEV"

# Issues assigned to current user
assignee = currentUser()

# Issues updated in last week
updated >= -7d

# Open issues in project
project = "DEV" AND status = "Open"

# Issues with specific priority
priority = High AND project = "DEV"
```

### Zephyr TQL Examples
```bash
# Test cases in specific project
projectKey = "JQA"

# Draft test cases
projectKey = "JQA" AND status = "Draft"

# Test cases by name
name ~ "login"

# Test cases in folder
projectKey = "JQA" AND folder = "/Functional Tests"
```

---