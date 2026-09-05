package com.personal.lld.appender;

import com.personal.lld.core.LogLevel;
import com.personal.lld.core.LogMessage;
import com.personal.lld.formatter.LogFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * File appender with append semantics and explicit resource lifecycle.
 */
public class FileAppender extends AbstractAppender {

    private final Path filePath;
    private final BufferedWriter writer;
    private final Object writeLock = new Object();

    public FileAppender(String filePath) {
        this(Path.of(filePath), LogLevel.DEBUG, null);
    }

    public FileAppender(String filePath, LogLevel level) {
        this(Path.of(filePath), level, null);
    }

    public FileAppender(Path filePath, LogLevel level, LogFormatter formatter) {
        super(level, formatter != null ? formatter : new com.personal.lld.formatter.SimpleFormatter());
        this.filePath = Objects.requireNonNull(filePath, "filePath");

        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            this.writer = Files.newBufferedWriter(
                    filePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize FileAppender for " + filePath, e);
        }
    }

    @Override
    public void append(LogMessage message) {
        if (!isEnabled(message.getLevel())) {
            return;
        }

        synchronized (writeLock) {
            try {
                writer.write(getFormatter().format(message));
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write log to " + filePath, e);
            }
        }
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void close() {
        synchronized (writeLock) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to close log file " + filePath, e);
            }
        }
    }
}
