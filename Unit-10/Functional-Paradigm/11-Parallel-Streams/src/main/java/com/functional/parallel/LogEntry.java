package com.functional.parallel;

import java.time.LocalDateTime;

/**
 * Immutable log entry from a web server access log.
 *
 * @param timestamp  when the request occurred
 * @param level      log level: INFO, WARN, ERROR
 * @param path       the requested URL path
 * @param statusCode HTTP response code
 * @param durationMs response time in milliseconds
 * @param userId     authenticated user ID, or null for anonymous
 */
public record LogEntry(LocalDateTime timestamp, String level, String path,
                       int statusCode, long durationMs, String userId) {

    public boolean isError()   { return statusCode >= 500; }
    public boolean isSlowRequest() { return durationMs > 500; }
}
