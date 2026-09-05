package com.personal.lld.core;

import com.personal.lld.appender.LogAppender;

import java.util.List;

public interface Logger extends AutoCloseable {

    void debug(String message);

    void info(String message);

    void warning(String message);

    void error(String message);

    void fatal(String message);

    void error(String message, Throwable throwable);

    void log(LogLevel level, String message);

    void log(LogLevel level, String message, Throwable throwable);

    void setLevel(LogLevel level);

    LogLevel getLevel();

    String getName();

    void addAppender(LogAppender appender);

    void removeAppender(LogAppender appender);

    List<LogAppender> getAppenders();

    @Override
    void close();
}
