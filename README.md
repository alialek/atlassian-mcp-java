# Confluence MCP Server Example

This repository contains the complete source code for our Confluence MCP Server example built with Spring Boot AI. The server integrates with Confluence Cloud and exposes various document management operations as callable tools using the **@Tool** annotation. You can test this server with any MCP client, including the Claude desktop app.

For a detailed explanation on how to build an MCP server, check out our blog:  
**[Build MCP Servers with Spring Boot AI: A Beginner’s Guide](https://bootcamptoprod.com/build-mcp-servers-with-spring-boot-ai/)**

---

## Features

- **Confluence Cloud Integration:** Connects to Confluence Cloud to manage spaces, pages, and document history.
- **Callable Tools:** Exposes operations like listing spaces and creating documents using the `@Tool` annotation.
- **Tool Registration:** Uses `ToolCallbackProvider` to register the available service methods with the MCP framework.
- **Testable with MCP Clients:** Easily test the server using any MCP client, including the Claude desktop app.

---