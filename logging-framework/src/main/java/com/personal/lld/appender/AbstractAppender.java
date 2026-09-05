package com.personal.lld.appender;

import com.personal.lld.core.LogLevel;
import com.personal.lld.formatter.LogFormatter;
import com.personal.lld.formatter.SimpleFormatter;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class AbstractAppender implements LogAppender {

    @Setter
    private volatile LogLevel level;

    @Setter
    private volatile LogFormatter formatter;

    protected AbstractAppender() {
        this(LogLevel.DEBUG, new SimpleFormatter());
    }

    protected AbstractAppender(LogLevel level, LogFormatter formatter) {
        this.level = level;
        this.formatter = formatter;
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return level != null && level.isGreaterOrEqual(this.level);
    }
}
