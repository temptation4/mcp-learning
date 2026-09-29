package com.learn.mcptimeserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class TimeTools {

    private static final Logger log =
            LoggerFactory.getLogger(TimeTools.class);

    @McpTool(
            name = "getCurrentTime",
            description = "Returns the current date and time for a given IANA time zone"
    )
    public String getCurrentTime(String timeZone) {
        ZoneId zoneId;

        try {
            zoneId = ZoneId.of(timeZone == null || timeZone.isBlank()
                    ? "Asia/Kolkata"
                    : timeZone.trim());
        } catch (Exception ex) {
            return "Invalid time zone: " + timeZone;
        }

        return ZonedDateTime.now(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }
    @McpTool(
            name = "add",
            description = "Adds two numbers using the time server"
    )
    public int add(
            @McpToolParam(required = true) int a,
            @McpToolParam(required = true) int b) {

        log.info("TIME SERVER add called: a={}, b={}", a, b);

        return a + b;
    }

    @Tool(description = "Returns the current date in the specified timezone")
    public String getCurrentDate(String timezone) {

        try {
            ZoneId zoneId = ZoneId.of(timezone);

            return LocalDate.now(zoneId).toString();

        } catch (Exception e) {
            return "Invalid timezone: " + timezone;
        }
    }

}
