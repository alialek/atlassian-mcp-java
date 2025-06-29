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

### Confluence Tools
- Search content using CQL
- List spaces and documents
- Create, read, update pages
- Manage page metadata and history

### Jira Tools  
- Search issues using JQL
- Create, read, update issues
- Manage projects and versions
- Add comments and work logs

### Zephyr Tools
- Manage test cases, plans, and runs
- Execute tests and record results
- Manage test steps and environments
- Search using TQL

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