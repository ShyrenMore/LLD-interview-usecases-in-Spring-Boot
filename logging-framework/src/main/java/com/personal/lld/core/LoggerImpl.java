package com.personal.lld.core;

import com.personal.lld.appender.ConsoleAppender;
import com.personal.lld.appender.LogAppender;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe logger implementation.
 *
 * Logger-level filtering happens before creating LogMessage, which avoids
 * unnecessary object allocation when the event is below the logger threshold.
 */
public class LoggerImpl implements Logger {

    private final String name;
    private volatile LogLevel level;
    private final CopyOnWriteArrayList<LogAppender> appenders = new CopyOnWriteArrayList<>();

    public LoggerImpl() {
        this("DefaultLogger", true);
    }

    public LoggerImpl(String name) {
        this(name, true);
    }

    public LoggerImpl(String name, boolean addDefaultAppender) {
        this.name = Objects.requireNonNull(name, "name");
        this.level = LogLevel.DEBUG;

        if (addDefaultAppender) {
            addAppender(new ConsoleAppender());
        }
    }

    public LoggerImpl(String name, LogConfiguration config) {
        this(name, true);
        if (config != null && config.getRootLevel() != null) {
            this.level = config.getRootLevel();
        }
    }

    @Override
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    @Override
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    @Override
    public void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    @Override
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    @Override
    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    @Override
    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Objects.requireNonNull(level, "level");

        if (!level.isGreaterOrEqual(this.level)) {
            return;
        }

        LogMessage logMessage = LogMessage.builder()
                .level(level)
                .message(message)
                .loggerName(name)
                .source(getCallingSource())
                .throwable(throwable)
                .build();

        for (LogAppender appender : appenders) {
            if (appender.isEnabled(level)) {
                appender.append(logMessage);
            }
        }
    }

    @Override
    public void setLevel(LogLevel level) {
        this.level = Objects.requireNonNull(level, "level");
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addAppender(LogAppender appender) {
        appenders.add(Objects.requireNonNull(appender, "appender"));
    }

    @Override
    public void removeAppender(LogAppender appender) {
        appenders.remove(appender);
    }

    @Override
    public List<LogAppender> getAppenders() {
        return List.copyOf(appenders);
    }

    @Override
    public void close() {
        for (LogAppender appender : appenders) {
            appender.close();
        }
    }

    private String getCallingSource() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        return walker.walk(frames -> frames
                .filter(frame -> !frame.getClassName().equals(LoggerImpl.class.getName()))
                .findFirst()
                .map(frame -> frame.getClassName() + "." + frame.getMethodName())
                .orElse("Unknown"));
    }
}
