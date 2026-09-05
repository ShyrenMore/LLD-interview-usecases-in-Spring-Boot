package com.personal.lld.core;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Immutable log event.
 */
@Getter
@Builder
public class LogMessage {
    @Builder.Default
    private final Instant timestamp = Instant.now();

    private final LogLevel level;
    private final String message;
    private final String loggerName;
    private final String source;
    private final Throwable throwable;
}
