package com.learn.mcpdb.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseTools {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Returns all tables in the configured MySQL database")
    public List<String> getTables() {
        return jdbcTemplate.queryForList("SHOW TABLES", String.class);
    }

    @Tool(description = "Returns schema information for a database table")
    public List<Map<String, Object>> getTableSchema(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name");
        }
        return jdbcTemplate.queryForList("DESCRIBE " + tableName);
    }
}
