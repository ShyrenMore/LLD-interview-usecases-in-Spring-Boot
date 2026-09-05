package com.personal.lld.service;

import com.personal.lld.appender.FileAppender;
import com.personal.lld.core.LogLevel;
import com.personal.lld.core.Logger;
import com.personal.lld.core.LoggerFactory;
import com.personal.lld.formatter.DetailedFormatter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class WrapperService implements CommandLineRunner {

    @Override
    public void run(String... args) {
        demoBasicLogging();
        demoMultipleAppenders();
    }

    private void demoBasicLogging() {
        Logger logger = LoggerFactory.getLogger("BasicLogger");
        logger.setLevel(LogLevel.DEBUG);

        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warning("This is a warning message");
        logger.error("This is an error message");
        logger.fatal("This is a fatal message");
    }

    private void demoMultipleAppenders() {
        Logger logger = LoggerFactory.getLogger("MultiAppenderLogger");

        Path logPath = Path.of("target", "demo.log");
        FileAppender fileAppender = new FileAppender(
                logPath,
                LogLevel.INFO,
                new DetailedFormatter()
        );

        logger.addAppender(fileAppender);

        logger.info("This message goes to both console and file");
        logger.error("This error also goes to both destinations");

        fileAppender.close();
        logger.removeAppender(fileAppender);
    }
}
