package com.personal.lld.appender;

import com.personal.lld.core.LogLevel;
import com.personal.lld.core.LogMessage;
import com.personal.lld.formatter.LogFormatter;

import java.io.PrintStream;

public class ConsoleAppender extends AbstractAppender {

    private final PrintStream outputStream;
    private final PrintStream errorStream;

    public ConsoleAppender() {
        this(LogLevel.DEBUG, System.out, System.err, null);
    }

    public ConsoleAppender(LogLevel level) {
        this(level, System.out, System.err, null);
    }

    public ConsoleAppender(LogLevel level, PrintStream outputStream, PrintStream errorStream, LogFormatter formatter) {
        super(level, formatter != null ? formatter : new com.personal.lld.formatter.SimpleFormatter());
        this.outputStream = outputStream;
        this.errorStream = errorStream;
    }

    @Override
    public void append(LogMessage message) {
        if (!isEnabled(message.getLevel())) {
            return;
        }

        String formatted = getFormatter().format(message);

        if (message.getLevel() == LogLevel.ERROR || message.getLevel() == LogLevel.FATAL) {
            errorStream.println(formatted);
        } else {
            outputStream.println(formatted);
        }
    }
}
