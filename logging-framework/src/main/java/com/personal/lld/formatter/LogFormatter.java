package com.personal.lld.formatter;

import com.personal.lld.core.LogMessage;

import java.time.format.DateTimeFormatter;

public interface LogFormatter {

    String format(LogMessage message);

    void setPattern(String pattern);

    String getPattern();

    void setDateFormat(String dateFormat);

    DateTimeFormatter getDateTimeFormatter();
}
