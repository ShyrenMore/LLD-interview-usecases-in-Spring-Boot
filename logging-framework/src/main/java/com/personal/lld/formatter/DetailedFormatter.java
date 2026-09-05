package com.personal.lld.formatter;

import com.personal.lld.core.LogMessage;

public class DetailedFormatter extends SimpleFormatter {

    public DetailedFormatter() {
        super("[%LEVEL] %TIMESTAMP [%LOGGER] [%SOURCE] - %MESSAGE");
    }

    public DetailedFormatter(String pattern) {
        super(pattern);
    }
}
