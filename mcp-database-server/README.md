# MCP Database Server

Spring Boot + Spring AI MCP server with two read-only database tools:
- getTables
- getTableSchema

Edit application.yml and set your MySQL password.

Default DB: test
Default port: 8083

Run:
mvn clean package
mvn spring-boot:run

MCP endpoint:
http://localhost:8083/mcp

Add to your MCP client:
database-server:
  url: http://localhost:8083
  endpoint: /mcp

This starter intentionally does not expose arbitrary SQL execution.
