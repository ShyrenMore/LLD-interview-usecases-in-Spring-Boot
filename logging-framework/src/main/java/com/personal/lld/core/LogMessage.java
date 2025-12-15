package com.personal.lld.core;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Represents a log message with timestamp, level, message, and optional source.
 * Immutable data class with builder pattern for construction.
 */
@Data
@Builder
public class LogMessage {
    private final Instant timestamp = Instant.now();
    private final LogLevel level;
    private final String message;
    private final String source;
}
