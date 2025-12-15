package com.personal.lld.core;


import com.personal.lld.appender.ConsoleAppender;
import com.personal.lld.appender.LogAppender;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main implementation of the Logger interface.
 * Provides thread-safe logging with support for multiple appenders and filters.
 */
@Getter
@ToString
public class LoggerImpl implements Logger {

    private final String name;

    @Setter
    private LogLevel level = LogLevel.DEBUG;

    private final List<LogAppender> appenders =
            Collections.synchronizedList(new ArrayList<>());

    // ---- Constructors ----

    public LoggerImpl() {
        this("DefaultLogger", true);
    }

    public LoggerImpl(String name) {
        this(name, true);
    }

    public LoggerImpl(String name, boolean addDefaultAppender) {
        this.name = name;

        if (addDefaultAppender) {
            addAppender(new ConsoleAppender());
        }
    }

    public LoggerImpl(String name, LogConfiguration config) {
        this(name, true);
        this.level = config.getRootLevel();
    }

    // ---- Logging APIs ----

    @Override
    public synchronized void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    @Override
    public synchronized void info(String message) {
        log(LogLevel.INFO, message);
    }

    @Override
    public synchronized void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    @Override
    public synchronized void error(String message) {
        log(LogLevel.ERROR, message);
    }

    @Override
    public synchronized void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    @Override
    public synchronized void log(LogLevel level, String message) {
        if (!level.isGreaterOrEqual(this.level)) {
            return;
        }

        LogMessage logMessage = LogMessage.builder()
                .level(level)
                .message(message)
                .source(getCallingClass())
                .build();

        for (LogAppender appender : appenders) {
            if (appender.isEnabled(level)) {
                appender.append(logMessage);
            }
        }
    }

    // ---- Mutators ----

    @Override
    public void addAppender(LogAppender appender) {
        appenders.add(appender);
    }

    // ---- Defensive Copies ----

    public List<LogAppender> getAppenders() {
        return new ArrayList<>(appenders);
    }

    // ---- Source Detection ----

    private String getCallingClass() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length > 3) {
                StackTraceElement e = stackTrace[3];
                return e.getClassName() + "." + e.getMethodName();
            }
        } catch (Exception ignored) {
        }
        return "Unknown";
    }
}
