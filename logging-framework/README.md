# Functional Requirements

Log Levels with Priority System:
- Support 5 log levels: DEBUG, INFO, WARNING, ERROR, FATAL
- Each level has a priority (DEBUG=1, INFO=2, WARNING=3, ERROR=4, FATAL=5)
- Only log messages with priority ≥ configured level
- Example: If level is set to WARNING, only WARNING, ERROR, and FATAL messages are logged:

Log Message Structure:
- Each log message contains: timestamp, level, message text, and optional source 
- Timestamp: When the log was created
- Level: Severity of the message
- Message: What happened
- Source: Which class/method generated the log (optional)

Multiple Output Destinations
- Console: Display logs in terminal/console (for development)
- File: Save logs to a file (for production)
- Database: Store logs in database (for analysis)
- Same log message can go to multiple destinations simultaneously

Configuration System
- Set logging level for entire application
- Choose which output destinations to use
- Configure formatting rules
- Simple configuration without complex filtering

Thread Safety
- Multiple threads can log simultaneously without data corruption
- No lost or mixed-up log messages
- Thread-safe operations for all logging components

Extensibility
- Easy to add new output destinations (email, network, cloud storage)
- Easy to add new log levels if needed
- Easy to add custom formatting

Message Formatting:
- Customize how log messages appear in output
- Control timestamp format, level display, and message layout
- Different formats for different destinations

Edge cases:
- Multiple threads logging simulataneously 
- Invalid log levels or configs 
- File system full while logging 
- Database connection failure during database logging 

# Step 2: Identify Core Entities

- LogLevel: Enum with values [DEBUG, INFO, WARNING, ERROR, FATAL], priority, isGreaterThanOrEqual(LogLevel)
- LogMessage: timestamp, level, message, source 
- LogConfiguration: rootLevel

<Now code Entities  inside domain/entity package>

# Step 3: Discuss Interaction Flows

Basic Logging Flow
- Application creates log message -> Logger processes message -> If message passes level check, Logger sends to output destinations -> Each destination writes the message

Configuration Flow (Real-time)
- Application sets LogConfiguration -> Logger updates its settings -> All future logs follow new configuration

Multi-threaded Flow
- Multiple threads create log messages simultaneously -> Thread-safe Logger processes each request -> Each destination handles concurrent writes safely

Formatting Flow
- LogMessage reaches destination -> Destination formats message -> Formatted message is written to output

# Step 4: Defines Class Structures & Relationships

Interfaces: 

````
Logger
void debug(String message)
void info(String message)
void warning(String message)
void error(String message)
void fatal(String message)
void log(LogLevel level, String message)
void setLevel(LogLevel level)
void addAppender(LogAppender appender)
void addFilter(LogFilter filter)
void removeFilter(LogFilter filter)
List<LogAppender> getAppenders()
List<LogFilter> getFilters()
````
````
LogAppender
void append(LogMessage message)
void setLevel(LogLevel level)
LogLevel getLevel()
boolean isEnabled(LogLevel level)
void setFormatter(LogFormatter formatter)
LogFormatter getFormatter()
````
````
LogFormatter
String format(LogMessage message)
void setPattern(String pattern)
String getPattern()
void setDateFormat(String dateFormat)
````
````
LogFilter
boolean shouldLog(LogMessage message)
void setLevel(LogLevel level)
LogLevel getLevel()
````
````
LogConfiguration
void setRootLevel(LogLevel level)
LogLevel getRootLevel()
````

Implementations:

ConsoleAppender implements LogAppender
- Writes to System.out/System.err based on level
- Uses formatter to format messages before output
FileAppender implements LogAppender
- Writes to specified file with timestamp
- Uses formatter to format messages before writing
DatabaseAppender implements LogAppender
- Writes to database table
- Uses formatter to format messages before storage
SimpleFormatter implements LogFormatter
- Default format: "[LEVEL] TIMESTAMP - MESSAGE"
- Configurable date format and pattern
DetailedFormatter implements LogFormatter
- Extended format: "[LEVEL] TIMESTAMP [SOURCE] - MESSAGE"
- Includes source information when available
LevelFilter implements LogFilter
- Filters messages based on minimum log level
- Only allows messages with level ≥ configured level
SourceFilter implements LogFilter
- Filters messages based on source/class name
- Can include or exclude specific packages/classes

<Create dummy code now in above classes>.

# Step 5: Implement Core Use Cases

BASIC LOGGING USE CASE: 
Application calls logger.info("message) -> LoggerImpl.log(LogLevel.INFO, "message") -> LogMessage.Builder().level(INFO).message("message").build() -> Check level.isGreaterThanOrEqual(loggerLevel)
For each appender: appender.isEnabled(level) -> appender.append(logMessage) -> appender.getFormatter().format(logMessage) -> Write formatted message 

CONFIGURATION USE CASE:
Application calls logger.setLevel(LogLevel.WARNING) -> LoggerImpl.setLevel(LogLevel.WARNING) updates configuration -> Future logger.log() calls use new level for filtering

MULTI-THREADED USE CASE:
Thread1: logger.info("msg1")  +  Thread2: logger.error("msg2")
LoggerImpl.log() uses synchronized keyword for thread safety -> 
Uses Collections.synchronizedList for appenders/filters -> 
Concurrent appender.append() calls are handled safely -> 
No data corruption occurs

FILTERING USE CASE:
LoggerImpl.log() creates LogMessage -> For each filter in filters list: filter.shouldLog(logMessage) -> If any filter returns false → message is dropped (return early) -> If all filters pass → proceed to appenders

FORMATTING USE CASE:
appender.append(logMessage) is called -> LogFormatter formatter = appender.getFormatter() -> String formatted = formatter.format(logMessage) -> Write formatted string to destination (console, file, database)


# Step 6: Explain Design Patterns and OOP principles applied

Design Pattern Used:
- Strategy Pattern – Used for different appenders (Console, File, Database) and formatters (Simple, Detailed)
- Chain of Responsibility Pattern – Implemented in the filter chain processing
- Builder Pattern – Used for constructing LogMessage objects

SOLID principles:
- Single Responsibility – Logger handles logging, Appenders handle output, Formatters handle formatting, Filters handle filtering
- Open/Closed – New appenders, formatters, and filters can be added without changing existing code
- Liskov Substitution – Any LogAppender, LogFormatter, or LogFilter can replace another
- Interface Segregation – Each interface only has the methods necessary for its responsibility
- Dependency Inversion – Logger depends on interfaces (LogAppender, LogFormatter, LogFilter) instead of concrete classes



# Step 7: Edge case (if you have time)

Multiple Threads Logging:
- Use synchronized methods or concurrent collections (e.g., ConcurrentLinkedQueue)
- Implement thread-safe appender implementations
- Use atomic operations for shared state (AtomicInteger, AtomicLong)
Invalid Log Levels:
- Validate inputs in the LogLevel enum (parse safely)
- Default to ERROR level for invalid inputs
- Return clear error messages or throw well-documented exceptions
File System Full:
- Wrap file operations in try-catch blocks
- Fallback to console logging or an in-memory buffer
- Emit alerts or escalate errors to monitoring systems
Database Connection Failure:
- Use connection pooling and retry logic with backoff
- Gracefully fallback to file or console logging
- Persist failed log writes in a retry queue for later flush
Invalid Format Patterns:
- Validate format patterns in formatter implementations
- Fallback to a safe/simple format when pattern is invalid
- Return clear error messages describing pattern syntax issues
Filter Configuration Errors:
- Validate filter parameters at registration time
- Default to accept-all behavior for invalid filters (fail-open)
- Handle filter exceptions gracefully so logging isn't disrupted

Implementation Strategies
- Thread Safety: Use synchronized blocks, concurrent collections, or lock-free structures where appropriate.
- Error Handling: Wrap critical operations in try-catch and provide fallbacks (console, file, retry-queue).
- Validation: Validate all configuration and public API inputs before applying them.
- Resource Management: Ensure proper cleanup (close file handles, DB connections) in appenders using try-with-resources or finally blocks.
- Filter Chain: Process filters sequentially and stop on first rejection; isolate filter exceptions to avoid breaking logging.
- Formatting: Use a template/pattern approach for message formatting and validate patterns up-front.
- Configuration: Apply configuration changes atomically (e.g., swap config object references) and validate before swap to avoid inconsistent state.

NOTE: just mention the handling, don't go and actually code for above unless asked 

Unique methods in this code:
- getCallingClass() 
- Filter and DatabaseAppender class is intentionally skipped to keep the code compact as part of interview 
