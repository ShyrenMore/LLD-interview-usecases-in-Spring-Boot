package com.personal.lld.formatter;

import com.personal.lld.core.LogMessage;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SimpleFormatter implements LogFormatter {

    private volatile String pattern;
    private volatile String dateFormat;
    private volatile DateTimeFormatter dateTimeFormatter;

    public SimpleFormatter() {
        this("[%LEVEL] %TIMESTAMP - %MESSAGE");
    }

    public SimpleFormatter(String pattern) {
        this.pattern = pattern;
        setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public String format(LogMessage message) {
        Objects.requireNonNull(message, "message");

        String timestamp = dateTimeFormatter
                .withZone(ZoneOffset.UTC)
                .format(message.getTimestamp());

        String source = message.getSource() != null ? message.getSource() : "";
        String logger = message.getLoggerName() != null ? message.getLoggerName() : "";

        return pattern
                .replace("%LEVEL", message.getLevel().toString())
                .replace("%TIMESTAMP", timestamp)
                .replace("%LOGGER", logger)
                .replace("%SOURCE", source)
                .replace("%MESSAGE", String.valueOf(message.getMessage()));
    }

    @Override
    public void setPattern(String pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void setDateFormat(String dateFormat) {
        this.dateFormat = Objects.requireNonNull(dateFormat, "dateFormat");
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    @Override
    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }
}
