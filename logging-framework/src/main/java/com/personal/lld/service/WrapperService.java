package com.personal.lld.service;

import com.personal.lld.appender.FileAppender;
import com.personal.lld.core.Logger;
import com.personal.lld.core.LoggerImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WrapperService implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Demo 1: Basic logging with different levels
         demoBasicLogging();

        // Demo 2: Multiple appenders
        demoMultipleAppenders();

    }

    private static void demoBasicLogging() {
        System.out.println("1. Basic Logging Demo:");
        System.out.println("----------------------");

        Logger logger = new LoggerImpl("BasicLogger");

        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warning("This is a warning message");
        logger.error("This is an error message");
        logger.fatal("This is a fatal message");

        System.out.println();
    }

    private static void demoMultipleAppenders() {
        System.out.println("2. Multiple Appenders Demo:");
        System.out.println("---------------------------");

        Logger logger = new LoggerImpl("MultiAppenderLogger");

        // Add file appender
        FileAppender fileAppender = new FileAppender("C:\\Users\\Shyren More\\OneDrive\\Desktop\\low-level-design-prep\\logging-framework\\src\\main\\resources\\demo.log");
        logger.addAppender(fileAppender);

        logger.info("This message goes to both console and file");
        logger.error("This error also goes to both destinations");

        System.out.println("Check 'demo.log' file for the logged messages");
        System.out.println();
    }
}
