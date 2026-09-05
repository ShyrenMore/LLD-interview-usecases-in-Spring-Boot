# Logging Framework — LLD Interview Revision Guide

A compact, interview-oriented Low-Level Design of a **logging framework** similar in spirit to Log4j/SLF4J-style architectures.

The original project was analyzed and improved without changing the core idea:

```text
Logger
  ↓
LogMessage
  ↓
Appender(s)
  ↓
Formatter
  ↓
Destination
```

The improved version additionally introduces:

- `LoggerFactory` for logger lifecycle/registry
- immutable log events
- `CopyOnWriteArrayList` for concurrent appender management
- explicit logger-level and appender-level filtering
- `StackWalker` for source detection
- `Path`/NIO-based file writing
- explicit appender resource lifecycle
- throwable support
- unit tests
- safer configuration defaults

---

# 1. 60-Second Interview Answer

> "I would design the logging framework around a `Logger` abstraction. A logger accepts log events at different severity levels. Before doing any expensive work, it checks whether the event is enabled.
>
> A `LogMessage` represents the immutable log event. The logger sends that event to one or more appenders. An appender represents the output destination—for example console, file, database, Kafka, or an external logging service.
>
> Formatting is separated from the destination using a `LogFormatter`, so the same event can be rendered as simple text, detailed text, JSON, etc.
>
> I use a `LoggerFactory` to maintain one logger instance per logger name. The framework is thread-safe because logging happens concurrently in real applications.
>
> The important extensibility points are the appenders and formatters. Adding a Kafka appender or JSON formatter should not require modifying the logger itself."

That is the core design.

---

# 2. Requirements

## Functional requirements

The framework should support:

```text
DEBUG
INFO
WARNING
ERROR
FATAL
```

A client should be able to:

```java
logger.debug("...");
logger.info("...");
logger.warning("...");
logger.error("...");
logger.fatal("...");
```

It should also support:

```java
logger.log(LogLevel.INFO, "...");
```

A logger can have multiple destinations:

```text
Console
File
Database
Kafka
Remote logging service
```

Formatting should be configurable.

---

## Non-functional requirements

Important interview requirements:

```text
Thread safety
Low overhead
Extensibility
Configurable log levels
Failure isolation
Resource management
Good performance
```

---

# 3. High-Level Architecture

```text
                  Application Code
                         │
                         ▼
                ┌─────────────────┐
                │     Logger      │
                └────────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │   LogMessage    │
                └────────┬────────┘
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
         Appender 1  Appender 2  Appender 3
              │          │          │
              ▼          ▼          ▼
           Console      File       Kafka
              │          │          │
              └──────────┼──────────┘
                         │
                   LogFormatter
```

A subtle implementation point:

The formatter belongs naturally to an appender because different destinations may want different formats.

Example:

```text
Console → simple text
File    → detailed text
Kafka   → JSON
```

---

# 4. Main Components

```text
Logger
LoggerImpl
LoggerFactory

LogMessage
LogLevel
LogConfiguration

LogAppender
AbstractAppender
ConsoleAppender
FileAppender

LogFormatter
SimpleFormatter
DetailedFormatter
```

---

# 5. Logger

`Logger` is the client-facing abstraction.

Responsibilities:

- accept logging requests
- apply logger-level filtering
- create `LogMessage`
- fan out the event to appenders
- manage appenders

Core idea:

```java
logger.info("Order created");
```

becomes:

```text
logger
  ↓
check INFO >= logger threshold
  ↓
create LogMessage
  ↓
iterate appenders
  ↓
appender-level filtering
  ↓
format
  ↓
write
```

---

# 6. Why `LoggerFactory`?

A common interview question is:

> Why don't we simply create `new LoggerImpl()` everywhere?

Because applications often want a logical logger per class/component.

Example:

```java
Logger logger = LoggerFactory.getLogger("PaymentService");
Logger logger = LoggerFactory.getLogger("OrderService");
```

The factory keeps a registry:

```text
PaymentService → Logger instance
OrderService   → Logger instance
```

In the improved implementation:

```java
ConcurrentHashMap<String, Logger>
```

is used.

The same name maps to the same logger instance.

```text
getLogger("PaymentService")
        ↓
computeIfAbsent
        ↓
one Logger instance
```

---

# 7. Logger Factory Pattern

This is essentially a registry/factory combination.

```text
             LoggerFactory
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
PaymentLogger            OrderLogger
```

The important interview point is not the exact class name.

The important point is:

> "We centralize logger creation and reuse loggers by logical name."

---

# 8. LogLevel

The framework defines:

```text
DEBUG   1
INFO    2
WARNING 3
ERROR   4
FATAL   5
```

If logger level is:

```text
INFO
```

then:

```text
DEBUG   → ignored
INFO    → logged
WARNING → logged
ERROR   → logged
FATAL   → logged
```

The comparison is:

```java
eventLevel.isGreaterOrEqual(configuredLevel)
```

---

# 9. Logger-Level vs Appender-Level Filtering

This is an important design detail.

There can be two filters:

```text
Logger level
      ↓
Appender level
```

Example:

```text
Logger threshold = DEBUG

Console threshold = INFO
File threshold    = ERROR
```

For:

```text
DEBUG message
```

nothing gets written.

For:

```text
INFO message
```

console receives it.

For:

```text
ERROR message
```

both console and file receive it.

This provides flexible routing.

---

# 10. Appender

`LogAppender` represents a destination.

Examples:

```text
ConsoleAppender
FileAppender
DatabaseAppender
KafkaAppender
ElasticsearchAppender
S3Appender
```

Interface:

```java
void append(LogMessage message);
```

The logger doesn't care where the event goes.

This follows the Open/Closed Principle.

---

# 11. Why an Appender Abstraction?

Without it:

```java
if (console) ...
if (file) ...
if (database) ...
if (kafka) ...
```

would grow inside `LoggerImpl`.

With an abstraction:

```text
Logger
  ↓
LogAppender
  ├── ConsoleAppender
  ├── FileAppender
  ├── KafkaAppender
  └── DatabaseAppender
```

New destinations do not require modifying logger logic.

---

# 12. Formatter

`LogFormatter` converts a `LogMessage` into a string.

Examples:

```text
SimpleFormatter
DetailedFormatter
JsonFormatter
XmlFormatter
```

This is another extensibility point.

---

# 13. Why Formatter Separately from Appender?

Destination and representation are different responsibilities.

For example:

```text
FileAppender
    +
SimpleFormatter
```

or:

```text
FileAppender
    +
DetailedFormatter
```

The same file destination can use different formats.

Likewise:

```text
ConsoleAppender + SimpleFormatter
KafkaAppender    + JsonFormatter
```

---

# 14. Formatting Flow

```text
LogMessage
     ↓
LogFormatter
     ↓
String
     ↓
Appender destination
```

Example:

```text
LogMessage:
level = ERROR
message = "Payment failed"
logger = PaymentService
source = PaymentService.charge
```

Simple output:

```text
[ERROR] 2026-01-01 10:00:00 - Payment failed
```

Detailed output:

```text
[ERROR] 2026-01-01 10:00:00 [PaymentService] [PaymentService.charge] - Payment failed
```

---

# 15. `LogMessage`

The improved implementation treats `LogMessage` as an immutable event.

It contains:

```text
timestamp
level
message
loggerName
source
throwable
```

The important design principle:

> Once a log event is created, appenders should observe the same event rather than mutate it.

This avoids surprising behavior when multiple appenders process the same event.

---

# 16. Throwable / Exception Logging

A useful extension is:

```java
logger.error("Payment failed", exception);
```

This is represented by:

```java
Throwable throwable
```

inside the `LogMessage`.

Production formatters can then print:

```text
message
+
exception class
+
exception message
+
stack trace
```

---

# 17. Thread Safety

Logging is heavily concurrent.

Consider:

```text
Thread 1 → logger.info(...)
Thread 2 → logger.error(...)
Thread 3 → logger.debug(...)
```

The framework must safely handle concurrent use.

The improved design uses:

```java
volatile LogLevel
CopyOnWriteArrayList<LogAppender>
```

for the mutable logger configuration/appender collection.

This avoids global synchronization around every log call.

---

# 18. Why Not Synchronize Every Logger Method?

The original implementation used `synchronized` on logging methods.

That is safe, but expensive:

```text
Thread A
   │
   ├── lock
   ├── create event
   ├── append
   └── unlock

Thread B
   │
   └── waits
```

Logging can be extremely high frequency.

The improved approach:

```text
CopyOnWriteArrayList
+
volatile configuration
+
stateless/mostly immutable event
```

allows more concurrency.

---

# 19. Why CopyOnWriteArrayList?

Appenders are usually:

```text
read frequently
write/configure infrequently
```

Every log call iterates over appenders.

Appender additions/removals are much rarer.

That makes:

```java
CopyOnWriteArrayList
```

a reasonable LLD choice.

Trade-off:

```text
Excellent read/iteration behavior
Expensive writes because an array copy occurs
```

This matches the expected usage pattern.

---

# 20. FileAppender

The improved file appender uses:

```java
java.nio.file.Files
java.nio.file.Path
BufferedWriter
```

It:

- creates parent directories if needed
- opens file in append mode
- uses UTF-8
- writes line-by-line
- flushes
- closes the resource explicitly

The original implementation used a hard-coded Windows path in the demo.

The improved version uses:

```text
target/demo.log
```

so the project is portable.

---

# 21. Resource Management

A file appender owns an operating-system resource.

Therefore:

```java
LogAppender extends AutoCloseable
```

and:

```java
logger.close()
```

can close resources.

This is a useful interview observation:

> "Any appender that owns a resource should have an explicit lifecycle."

---

# 22. What Happens if File Logging Fails?

This is an important production discussion.

Possible choices:

### Option 1 — Fail the application

Usually undesirable.

### Option 2 — Drop the log

Possible, but observability is lost.

### Option 3 — Fallback

Example:

```text
FileAppender fails
      ↓
fallback to console
```

### Option 4 — Internal error channel

Send appender failures to:

```text
stderr
monitoring
metrics
internal logging
```

The current educational implementation throws an exception from file writes so failure is explicit. A production framework would usually isolate appender failure and continue with other appenders.

---

# 23. Fan-Out

One logger can send one event to many appenders.

```text
                Logger
                  │
            LogMessage
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
    Console      File       Kafka
```

This is a classic fan-out model.

---

# 24. One Important Production Improvement: Async Logging

The sample framework is synchronous:

```text
Application thread
      ↓
logger
      ↓
file write
      ↓
return
```

File/network I/O can be slow.

Production logging frameworks often use:

```text
Application Thread
        ↓
   in-memory queue
        ↓
   background worker
        ↓
      appenders
```

Benefits:

- lower latency for application threads
- smoother bursts
- batching opportunities

Trade-offs:

- queue memory
- event loss on crash depending on durability
- shutdown complexity
- backpressure

---

# 25. Async Architecture

```text
                  Application
                      │
                      ▼
                    Logger
                      │
                      ▼
                BlockingQueue
                      │
                      ▼
              Background Worker
                 │     │     │
                 ▼     ▼     ▼
              Console File  Kafka
```

A strong interview answer should mention this when asked about scalability.

---

# 26. Bounded Queue + Backpressure

Never assume logging traffic is infinite.

Use:

```text
bounded queue
```

Then define the policy when full:

```text
DROP DEBUG
BLOCK producer
DROP oldest
DROP newest
SYNC FALLBACK
```

A sensible priority policy might be:

```text
DEBUG → first candidate for dropping
INFO  → maybe droppable
WARN  → preserve
ERROR → preserve
FATAL → strongly preserve
```

The exact policy depends on system requirements.

---

# 27. Performance Optimization

Important optimizations:

```text
1. Check log level before creating LogMessage
2. Avoid expensive string construction for disabled logs
3. Async appenders
4. Batching
5. Buffered I/O
6. Reuse immutable formatter configuration
7. Minimize lock contention
```

The improved `LoggerImpl` performs the threshold check before constructing the `LogMessage`.

---

# 28. Lazy Message Construction

One issue not fully solved by a simple:

```java
logger.debug("value = " + expensiveCalculation());
```

is that `expensiveCalculation()` runs even if DEBUG is disabled.

A production API can support:

```java
logger.debug(() -> "value = " + expensiveCalculation());
```

Then:

```text
DEBUG enabled?
   │
   ├── NO → supplier never evaluated
   │
   └── YES → evaluate message
```

This is a useful performance extension to mention.

---

# 29. Structured Logging

Plain text is useful for humans.

Modern systems often want:

```json
{
  "timestamp": "...",
  "level": "ERROR",
  "logger": "PaymentService",
  "source": "PaymentService.charge",
  "message": "Payment failed",
  "traceId": "abc123"
}
```

This enables searching and aggregation.

Add:

```java
JsonFormatter implements LogFormatter
```

without changing `LoggerImpl`.

---

# 30. Context / Correlation IDs

In distributed systems, a log message should often contain:

```text
traceId
spanId
requestId
userId
serviceName
```

The logger can accept contextual data:

```text
MDC / ThreadLocal / explicit LogContext
```

Example:

```text
traceId=7f23
requestId=req-123
```

Every log event within that request can then be correlated.

---

# 31. Logger Hierarchy

A more advanced logging framework can support hierarchical names:

```text
com.company
com.company.payment
com.company.payment.refund
```

Configuration could be:

```text
root = INFO
com.company.payment = DEBUG
```

Then the specific logger inherits the root configuration unless overridden.

This becomes important in a Log4j-style system.

---

# 32. Configuration Model

Current model:

```java
LogConfiguration
    └── rootLevel
```

A production framework may support:

```text
root level
per-logger level
appenders
formatter
file path
rotation policy
async/sync
queue size
```

Example:

```text
root = INFO

PaymentService = DEBUG

Console = INFO
File = DEBUG
```

---

# 33. Log Rotation

A real file appender should not allow:

```text
application.log → 500 GB
```

Add rolling policies:

```text
size-based
time-based
size + time
```

Example:

```text
application.log
application.log.1
application.log.2
...
```

Also configure:

```text
retention
compression
maximum history
```

---

# 34. Failure Isolation

Suppose:

```text
ConsoleAppender succeeds
FileAppender fails
KafkaAppender succeeds
```

The logger should generally not lose all logging because one destination failed.

A stronger architecture:

```text
Logger
  │
  ├── Console → success
  ├── File    → failure → report internally
  └── Kafka   → success
```

This is especially important in production.

---

# 35. Security Considerations

Logs can accidentally contain sensitive information.

Never blindly log:

```text
password
PIN
CVV
full card number
API secret
access token
session token
```

The logging framework can support:

```text
masking
redaction
field filtering
PII policies
```

Example:

```text
4111111111111111
        ↓
4111********1111
```

The exact policy belongs to application/security requirements.

---

# 36. SOLID Principles

## Single Responsibility

```text
Logger
→ logging orchestration

Appender
→ destination

Formatter
→ representation

LogMessage
→ event data

LoggerFactory
→ logger lifecycle
```

Each component has a focused responsibility.

---

## Open/Closed Principle

Add:

```text
KafkaAppender
```

without changing `LoggerImpl`.

Add:

```text
JsonFormatter
```

without changing `FileAppender`.

---

## Dependency Inversion

`LoggerImpl` depends on:

```java
LogAppender
```

instead of:

```text
ConsoleAppender
FileAppender
KafkaAppender
```

directly.

---

# 37. Design Patterns Present

### Strategy-like composition

Different formatters and appenders provide interchangeable behavior.

```text
LogFormatter
   ├── SimpleFormatter
   └── DetailedFormatter
```

```text
LogAppender
   ├── ConsoleAppender
   └── FileAppender
```

### Factory

```text
LoggerFactory
```

creates/reuses loggers.

### Observer-like fan-out

A logger publishes an event to many appenders.

```text
Logger
 ├── Appender A
 ├── Appender B
 └── Appender C
```

Calling it Observer is conceptually useful, though `Appender` is more naturally a destination/sink abstraction.

---

# 38. Original Code — Important Problems Identified

The original code was a good starting LLD, but these points were worth fixing.

## 1. No LoggerFactory

Every caller could instantiate:

```java
new LoggerImpl("PaymentLogger")
```

leading to uncontrolled logger creation.

Fixed with:

```java
LoggerFactory.getLogger(name)
```

---

## 2. Logger methods were synchronized

This created unnecessary contention.

Fixed with concurrent collections/volatile configuration.

---

## 3. `Collections.synchronizedList`

The old implementation used synchronized list + synchronized logging methods.

The combination was heavier than necessary.

Replaced with:

```java
CopyOnWriteArrayList
```

---

## 4. `LogMessage` claimed immutability but used `@Data`

`@Data` generates setters for mutable fields unless fields are final.

The improved version uses:

```java
@Getter
@Builder
```

with final fields.

---

## 5. Formatter date format wasn't actually used

The old formatters created:

```java
DateTimeFormatter
```

but formatting used:

```java
timestamp.toString()
```

instead.

The improved formatter actually applies the configured date format.

---

## 6. File path was hard-coded to one developer's machine

The demo used a machine-specific Windows path.

Replaced with:

```text
target/demo.log
```

---

## 7. FileAppender error handling was weak

The old implementation printed errors to `System.err`.

The improved version makes initialization/write failures explicit.

For production, appender failure isolation should be added.

---

## 8. Missing exception support

Added:

```java
error(String message, Throwable throwable)
```

and `Throwable` to the event.

---

## 9. Missing explicit appender lifecycle

File writers need closing.

Added:

```java
AutoCloseable
```

support.

---

# 39. Class Diagram

```text
                     ┌──────────────────┐
                     │     Logger       │
                     └────────┬─────────┘
                              │
                              │ implemented by
                              ▼
                     ┌──────────────────┐
                     │   LoggerImpl     │
                     └────────┬─────────┘
                              │
                creates       │
                              ▼
                     ┌──────────────────┐
                     │   LogMessage     │
                     └──────────────────┘
                              │
                         sent to
                              │
                 ┌────────────┼────────────┐
                 ▼            ▼            ▼
           ┌──────────┐ ┌──────────┐ ┌──────────┐
           │Appender  │ │Appender  │ │Appender  │
           │ Console  │ │  File    │ │  Kafka*  │
           └────┬─────┘ └────┬─────┘ └──────────┘
                │             │
                ▼             ▼
           Formatter      Formatter
                │             │
                ▼             ▼
             Console          File
```

`KafkaAppender` is a production extension, not part of the current sample.

---

# 40. Main Runtime Flow

```text
logger.error("Payment failed")
          │
          ▼
LoggerImpl.log(ERROR, ...)
          │
          ▼
Is ERROR >= logger level?
          │
          ├── NO → return
          │
          ▼ YES
Create LogMessage
          │
          ▼
Iterate appenders
          │
          ▼
Is ERROR >= appender level?
          │
          ├── NO → skip
          │
          ▼ YES
formatter.format(...)
          │
          ▼
append(...)
```

This is the flow to draw on a whiteboard.

---

# 41. Interview Questions

## Q1. Why have both Logger and Appender?

Because:

```text
Logger = logging API + orchestration
Appender = destination
```

Separating them lets one logger write to multiple destinations.

---

## Q2. Why separate Formatter?

Because destination and representation are independent concerns.

---

## Q3. Why use LoggerFactory?

To centralize logger creation and avoid unnecessary duplicate logger instances.

---

## Q4. Why not use a Singleton Logger?

You could create a global singleton, but that loses useful logical separation by logger name.

Better:

```text
LoggerFactory
   ↓
one Logger per name
```

rather than:

```text
one Logger for entire application
```

---

## Q5. Why is logging thread-safe?

Many application threads share the same logger.

Use:

```text
concurrent collection
volatile configuration
immutable events
```

---

## Q6. Why not synchronize the entire logger?

It serializes all logging through one lock and can create a bottleneck.

---

## Q7. What happens if file logging is slow?

Use asynchronous appenders with a bounded queue.

---

## Q8. What happens if queue is full?

Define a backpressure/drop policy.

For example:

```text
drop DEBUG first
preserve ERROR/FATAL
```

---

## Q9. What if a file appender fails?

Prefer isolating that failure so other appenders continue working.

---

## Q10. How would you add JSON logging?

Implement:

```java
JsonFormatter implements LogFormatter
```

No change to logger logic.

---

## Q11. How would you add Kafka?

Implement:

```java
KafkaAppender implements LogAppender
```

No change to logger logic.

---

## Q12. How would you add log rotation?

Enhance `FileAppender` with a rolling policy or introduce:

```text
RollingFileAppender
RotationPolicy
```

---

## Q13. How would you avoid expensive debug messages?

Use lazy message evaluation:

```java
logger.debug(() -> expensiveMessage());
```

---

## Q14. How do distributed systems correlate logs?

Add:

```text
traceId
spanId
requestId
```

using context propagation/MDC.

---

# 42. Strong Follow-Up Extensions

When interviewer says:

> "Now make it production-ready."

Discuss:

```text
1. Async logging
2. Bounded queue
3. Backpressure
4. JSON formatter
5. Structured fields
6. Logger hierarchy
7. Per-package configuration
8. Rolling file appender
9. Retention policies
10. Failure isolation
11. Metrics
12. Trace/request correlation
13. PII masking
14. Sampling
15. Batch writes
```

---

# 43. Potential Bottlenecks

## Synchronous disk I/O

```text
Application thread
     ↓
disk write
```

can increase application latency.

Solution:

```text
queue + background worker
```

---

## Lock contention

A global lock around logging can reduce throughput.

Solution:

```text
lock-free/concurrent structures
```

where appropriate.

---

## Formatting cost

Expensive JSON/stack-trace formatting can consume CPU.

Solution:

```text
filter first
format later
only for enabled appenders
```

---

## Network destination

Kafka/HTTP logging introduces network latency and failure modes.

Solution:

```text
async buffering
retry
batching
timeouts
circuit breaking
```

---

# 44. Reliability Trade-offs

Logging itself should not become the reason the application fails.

This creates a fundamental trade-off:

```text
More durability
      ↕
More latency/resource usage
```

For example:

### Synchronous durable logging

```text
more reliable
slower
```

### Async best-effort logging

```text
faster
possible log loss
```

The correct choice depends on whether logs are:

```text
debug/observability data
```

or:

```text
audit/compliance records
```

Audit logs often require a separate, more durable architecture rather than ordinary application logging.

---

# 45. Logging vs Audit Trail

This is a very useful interview distinction.

### Logging

Primarily for:

```text
debugging
observability
operations
diagnostics
```

### Audit

Primarily for:

```text
compliance
security
non-repudiation
business history
```

Do not rely on an ordinary text log file as the authoritative financial/business audit ledger.

---

# 46. Testing

## Unit tests

Test:

```text
log-level filtering
appender filtering
formatter output
logger factory reuse
file writing
exception logging
```

## Concurrency tests

Test:

```text
many threads
same logger
same appender
```

## Failure tests

Test:

```text
file unavailable
disk full
formatter failure
network appender timeout
queue overflow
```

---

# 47. Last-Minute Mental Model

Memorize this:

```text
LOGGER
→ entry point

LOG MESSAGE
→ immutable event

APPENDER
→ WHERE does it go?

FORMATTER
→ WHAT does it look like?

LOGGER FACTORY
→ WHICH logger instance do I use?

LOG LEVEL
→ SHOULD it be emitted?
```

The entire framework can be remembered as:

```text
                 SHOULD?
                   │
                   ▼
                Logger
                   │
                   ▼
              LogMessage
                   │
              WHERE?
          ┌────────┼────────┐
          ▼        ▼        ▼
       Console    File     Kafka
          │        │        │
          ▼        ▼        ▼
      Formatter Formatter Formatter
```

---

# 48. 30-Second Pre-Interview Revision

```text
Requirements:
DEBUG / INFO / WARN / ERROR / FATAL
multiple destinations
multiple formats
thread-safe
extensible

CORE:
Logger
LogMessage
Appender
Formatter
LoggerFactory

PATTERNS:
Factory/Registry
Strategy-like interchangeable appenders/formatters
Observer-like fan-out

FLOW:
Logger
→ level check
→ LogMessage
→ appenders
→ appender-level filter
→ formatter
→ destination

THREAD SAFETY:
ConcurrentHashMap
CopyOnWriteArrayList
volatile configuration
immutable LogMessage

PRODUCTION:
async queue
backpressure
JSON/structured logs
rotation
retention
traceId
PII masking
failure isolation
sampling

KEY DESIGN PRINCIPLE:
Logger should not know HOW or WHERE a message is written.
```

---

# 49. Final Interview Answer

> "The key to the logging framework is separation of concerns. `Logger` exposes the API and performs orchestration, `LogMessage` represents the event, `Appender` abstracts the destination, and `Formatter` abstracts representation.
>
> I use a `LoggerFactory` to manage loggers by name and a concurrent registry to reuse them. Log-level filtering happens before event creation where possible, and each appender can have its own threshold.
>
> The design is extensible: I can add a JSON formatter or Kafka appender without changing `LoggerImpl`.
>
> For production, I'd make the logging pipeline asynchronous using a bounded queue, add batching and backpressure, support structured/contextual logging with trace IDs, rolling files, failure isolation, and sensitive-data masking. The most important trade-off is balancing logging durability against application latency and resource usage."

---

# 50. Final Takeaway

The reusable LLD lessons are:

```text
Separate API from destination.

Separate destination from formatting.

Centralize object creation/reuse.

Filter early.

Prefer immutable events.

Design for concurrency.

Avoid making synchronous I/O part of the critical application path.

Make extension points explicit.

Treat failures and backpressure as first-class design concerns.

Keep audit/compliance data separate from ordinary diagnostic logging.
```
