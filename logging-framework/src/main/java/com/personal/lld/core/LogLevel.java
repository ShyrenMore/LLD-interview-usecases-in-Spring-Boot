package com.personal.lld.core;

/**
 * Severity of a log event.
 * Higher priority means greater severity.
 */
public enum LogLevel {
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    FATAL(5);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isGreaterOrEqual(LogLevel other) {
        return this.priority >= other.priority;
    }
}
