package com.personal.lld.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central logger registry. Ensures one logger instance per name.
 */
public final class LoggerFactory {

    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();
    private static volatile LogConfiguration configuration = new LogConfiguration();

    private LoggerFactory() {
    }

    public static Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(
                name,
                key -> new LoggerImpl(key, configuration)
        );
    }

    public static void setConfiguration(LogConfiguration newConfiguration) {
        configuration = newConfiguration != null ? newConfiguration : new LogConfiguration();
    }

    public static void clear() {
        LOGGERS.values().forEach(Logger::close);
        LOGGERS.clear();
    }
}
