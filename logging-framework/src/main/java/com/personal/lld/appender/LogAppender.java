package com.personal.lld.appender;

import com.personal.lld.core.LogLevel;
import com.personal.lld.core.LogMessage;
import com.personal.lld.formatter.LogFormatter;

/**
 * Destination for log events.
 */
public interface LogAppender extends AutoCloseable {

    void append(LogMessage message);

    void setLevel(LogLevel level);

    LogLevel getLevel();

    boolean isEnabled(LogLevel level);

    void setFormatter(LogFormatter formatter);

    LogFormatter getFormatter();

    @Override
    default void close() {
        // No-op for appenders that do not own resources.
    }
}
