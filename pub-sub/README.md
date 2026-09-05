# Publish/Subscribe LLD — Interview Revision Guide

## 1. What This System Is

This is a publish/subscribe messaging system where:

- Publishers publish messages to topics.
- Subscribers subscribe to topics.
- Messages can be delivered through multiple channels.
- The current channels are email and real-time.
- Subscriber online/offline state affects real-time delivery.
- Message delivery can be tracked and acknowledged.
- Publishing triggers asynchronous notification processing.

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Domain
```

The key design pattern is the **Observer Pattern**.

---

## 2. Overall Architecture

```text
                     ┌─────────────────────┐
                     │      Publisher      │
                     └──────────┬──────────┘
                                │
                                ▼
                     PublisherController
                                │
                                ▼
                       PublisherService
                                │
                    ┌───────────┴───────────┐
                    │                       │
                    ▼                       ▼
              TopicRepository       MessageRepository
                    │
                    ▼
                   Topic
                    │
                    ▼
             MessageSubject
              /            \
             /              \
            ▼                ▼
    EmailSubscriber   RealtimeSubscriber
```

Subscriber management:

```text
SubscriberController
        ↓
SubscriberService
        ↓
SubscriberRepository
        ↓
Subscriber
```

Topic subscriptions:

```text
SubscriptionController
        ↓
SubscriptionService
        ↓
SubscriptionRepository
```

Message acknowledgement:

```text
MessageController
        ↓
MessageService
        ↓
MessageDeliveryRepository
```

---

## 3. Main Domain Objects

### Message

Represents a published message:

```text
id
topicId
content
timestamp
```

A message belongs to one topic.

### Topic

Represents a logical channel:

```text
id
name
active
createdAt
messageSubject
```

The topic owns a `MessageSubject`, which demonstrates the Observer Pattern.

### Subscriber

Represents a recipient/device:

```text
id
email
realtimeConnectionId
online
createdAt
lastHeartbeat
```

### Subscription

Represents the relationship between a subscriber and topic:

```text
id
topicId
subscriberId
active
createdAt
```

### MessageDelivery

Tracks delivery for a specific message/subscriber/channel:

```text
id
messageId
subscriberId
channel
status
createdAt
acknowledgedAt
```

---

## 4. Topic vs Subscription

This distinction is commonly asked.

### Topic

The publication channel:

```text
Technology
News
Sports
```

### Subscription

The relationship:

```text
Alice → Technology
Bob   → Technology
Alice → News
```

So this is effectively a many-to-many relationship:

```text
Subscriber * ←→ * Topic
```

with `Subscription` as the relationship entity.

---

## 5. Message vs MessageDelivery

These represent different concepts.

```text
Message
→ "What was published?"

MessageDelivery
→ "What happened when this message was delivered to this subscriber?"
```

One message can generate multiple delivery records:

```text
Message M1
 ├── Alice / EMAIL
 ├── Alice / REALTIME
 ├── Bob   / EMAIL
 └── Bob   / REALTIME
```

This is an important modeling decision.

---

# 6. Observer Pattern

The main design pattern is Observer.

```text
Subject
   ↓
MessageSubject

Observer
   ↓
SubscriberObserver

Concrete Observers
   ├── EmailSubscriber
   └── RealtimeSubscriber
```

The subject maintains:

```text
emailSubscribers
realtimeSubscribers
```

and exposes operations such as:

```java
addEmailSubscriber(...)
removeEmailSubscriber(...)

addRealtimeSubscriber(...)
removeRealtimeSubscriber(...)

notify(...)
```

---

## 7. Observer Flow

When a message is published:

```text
PublisherService
      ↓
Topic
      ↓
MessageSubject.notify(message)
      ↓
 ┌────┴─────┐
 ▼          ▼
Email      Realtime
Observer   Observer
 ▼          ▼
Email      Push/Socket
```

Each observer receives:

```java
update(Message message)
```

and handles the message according to its channel.

---

## 8. Why Observer Pattern?

Without Observer:

```text
PublisherService
 ├── EmailService
 ├── PushService
 ├── SMSService
 └── WebSocketService
```

This creates tight coupling.

With Observer:

```text
MessageSubject
      ↓
SubscriberObserver
      ├── EmailSubscriber
      ├── RealtimeSubscriber
      └── Future Subscriber
```

### Interview Answer

> "I used the Observer Pattern because one published message can have multiple independent consumers. The subject only knows the observer abstraction, while each observer encapsulates its notification behavior. This reduces coupling and makes it easier to add new channels."

---

# 9. Adding Another Notification Channel

Suppose SMS is required.

Create:

```java
SmsSubscriber implements SubscriberObserver
```

Then register it with the subject.

The publishing flow does not need to know the SMS implementation.

This demonstrates the Open/Closed Principle.

---

# 10. Observer vs Event-Driven Architecture

The current implementation uses an **in-process Observer Pattern**.

A production system can evolve to:

```text
Publisher Service
      ↓
Event / Message Broker
      ↓
 ┌────┼───────────┐
 ▼    ▼           ▼
Email Realtime   Analytics
Worker Worker    Consumer
```

Possible technologies:

```text
Kafka
SNS/SQS
RabbitMQ
Pulsar
```

### Interview Answer

> "Observer is useful for modeling the LLD and in-process decoupling. For a distributed production system, I'd prefer durable events through a broker because they provide better scalability, retries, isolation, and fault tolerance."

---

# 11. Publishing Flow

`PublisherService.publishMessage()` performs:

```text
1. Find topic
2. Verify topic exists
3. Verify topic is active
4. Create message
5. Persist message
6. Trigger asynchronous notification processing
7. Return message
```

Conceptually:

```text
PublisherController
        ↓
PublisherService
        ↓
TopicRepository
        ↓
Create Message
        ↓
MessageRepository
        ↓
Async Delivery
        ↓
MessageSubject
        ↓
Observers
```

---

# 12. Why Asynchronous Processing?

The current implementation uses:

```java
CompletableFuture.runAsync(...)
```

The idea is:

```text
Request
  ↓
Persist message
  ↓
Return quickly

Notification
  ↓
processed asynchronously
```

This avoids making the publish request wait for all notification work.

### Interview Answer

> "Publishing and notification have different latency characteristics, so I separate persistence from delivery. The publisher should not have to wait for slower notification operations."

---

# 13. Important Limitation of `CompletableFuture.runAsync()`

Using:

```java
CompletableFuture.runAsync(...)
```

is fine for a simple simulation.

For production, you usually want a controlled execution model:

```text
Dedicated Executor
Thread Pool
Queue
Message Broker
```

because you need control over:

```text
thread count
queue size
backpressure
retries
shutdown
monitoring
```

Also, asynchronous execution by itself does **not** make the work durable.

If the process crashes before delivery finishes, the in-memory task may be lost.

---

# 14. Subscriber Lifecycle

A subscriber can go:

```text
ONLINE
OFFLINE
```

When online:

```text
subscriberId
connectionId
lastHeartbeat
```

can be updated.

The subscriber's active topic subscriptions can then register real-time observers.

---

# 15. Online Flow

```text
SubscriberService.goOnline()
        ↓
Update online status
        ↓
Find active subscriptions
        ↓
Find each topic
        ↓
Create RealtimeSubscriber
        ↓
Register observer
        ↓
Push pending deliveries
```

---

# 16. Offline Flow

```text
SubscriberService.goOffline()
        ↓
Update online = false
        ↓
Find subscriptions
        ↓
Remove realtime observer
```

The current LLD has a TODO for removing the exact observer instance.

A better implementation would identify observers using:

```text
subscriberId
connectionId
```

rather than attempting:

```java
removeRealtimeSubscriber(null);
```

---

# 17. Subscription Flow

```text
SubscriptionController
        ↓
SubscriptionService
        ↓
Create Subscription
        ↓
Persist Subscription
        ↓
Find Topic
        ↓
Find Subscriber
        ↓
Add EmailSubscriber
        ↓
If online
        ↓
Add RealtimeSubscriber
```

This means a subscriber can receive a topic through multiple channels.

---

# 18. Message Acknowledgement

The intended design is:

```text
MessageController
        ↓
MessageService
        ↓
Find MessageDelivery
        ↓
Update status
        ↓
ACKNOWLEDGED
        ↓
Set acknowledgedAt
```

Current model:

```text
PENDING
   ↓
DELIVERED
   ↓
ACKNOWLEDGED
```

The current `MessageService` has a TODO for locating the exact delivery record.

### Honest Interview Answer

> "The model supports delivery acknowledgement, but the repository lookup that maps message ID and subscriber ID to the delivery record is still a placeholder in this implementation."

---

# 19. Delivery Status

Current statuses:

```text
PENDING
DELIVERED
ACKNOWLEDGED
```

A real implementation could add:

```text
FAILED
RETRYING
EXPIRED
```

if those states become operationally useful.

---

# 20. Delivery Channel

Current:

```text
EMAIL
REALTIME
```

Possible extensions:

```text
SMS
PUSH
WHATSAPP
WEBHOOK
```

The interface-based Observer design makes additional delivery implementations straightforward.

---

# 21. Repository Layer

Repositories are currently in-memory implementations using:

```java
ConcurrentHashMap
```

Repositories:

```text
TopicRepository
SubscriberRepository
SubscriptionRepository
MessageRepository
MessageDeliveryRepository
```

Their purpose is to abstract persistence.

The service layer should not care whether the data is stored in:

```text
ConcurrentHashMap
SQL
MongoDB
DynamoDB
Redis
```

---

# 22. Why `ConcurrentHashMap`?

A normal:

```java
HashMap
```

is not safe for concurrent access.

`ConcurrentHashMap` provides much better behavior for concurrent reads/writes.

However:

> `ConcurrentHashMap` does not automatically make multi-step business operations atomic.

For example:

```text
read
→ check
→ modify
→ write
```

may still require synchronization/transactional control.

---

# 23. Spring Boot Conversion

The converted application uses:

```java
@RestController
@Service
@Repository
@RequiredArgsConstructor
@Slf4j
```

### Controller

Handles HTTP.

### Service

Handles business workflow.

### Repository

Handles data access.

### Lombok

Removes repetitive boilerplate.

---

# 24. Constructor Injection

Example:

```java
private final TopicRepository topicRepository;
```

with:

```java
@RequiredArgsConstructor
```

Spring injects the dependency through the generated constructor.

### Interview Answer

> "Constructor injection makes dependencies explicit, supports final fields, improves testability, and avoids hidden dependencies."

---

# 25. Controller Responsibilities

Controllers should stay thin:

```text
HTTP request
    ↓
Controller
    ↓
Service
```

They should not contain:

```text
notification logic
repository implementation
business rules
delivery algorithms
```

The service layer should own the workflow.

---

# 26. Domain vs Infrastructure

An important design observation:

The current `Topic` contains:

```java
MessageSubject
```

and the subject contains:

```text
EmailSubscriber
RealtimeSubscriber
```

This is good for demonstrating Observer Pattern.

However, in a production architecture, you may want the domain model to avoid holding infrastructure-specific objects such as:

```text
WebSocket connection
Email client
Push client
```

Instead:

```text
Topic
   ↓
Subscriptions
   ↓
Notification Service
   ↓
Delivery Infrastructure
```

---

# 27. Important Production Improvement: Durable Delivery

Current flow:

```text
Save Message
      ↓
CompletableFuture
      ↓
Notify observers
```

Failure scenario:

```text
Message saved
      ↓
Application crashes
      ↓
Notification never happens
```

A more reliable architecture:

```text
Database Transaction
   ├── Message
   └── Outbox Event
          ↓
     Outbox Publisher
          ↓
      Message Broker
          ↓
    Delivery Consumers
```

---

# 28. Transactional Outbox Pattern

The Outbox Pattern helps solve:

```text
Database update succeeds
BUT
event publishing fails
```

Instead:

```text
same database transaction
 ├── save message
 └── save event
```

Then a separate process publishes the event.

This gives much better reliability.

---

# 29. Retry Handling

Suppose email delivery fails.

A production delivery lifecycle could be:

```text
PENDING
   ↓
Attempt
   ↓
Success → DELIVERED
   ↓
Failure
   ↓
Retry
   ↓
Retry limit exceeded
   ↓
Dead Letter Queue
```

Useful concepts:

```text
retry
exponential backoff
dead-letter queue
idempotency
delivery status
observability
```

---

# 30. Idempotency

This is a very important distributed-systems concept.

Suppose delivery is retried:

```text
same message
same subscriber
same channel
```

We don't want:

```text
5 duplicate emails
```

A delivery should have a stable identity such as:

```text
deliveryId
```

or:

```text
messageId + subscriberId + channel
```

and the consumer should make repeated processing safe.

### Interview Answer

> "Because delivery is asynchronous and can be retried, I would use at-least-once delivery with idempotent consumers so retries do not create duplicate externally visible effects."

---

# 31. Fan-Out Problem

Suppose a topic has:

```text
10 million subscribers
```

A single publish cannot synchronously iterate through everyone.

This becomes a **fan-out problem**.

Two common models:

## Fan-Out on Write

```text
Publish Message
      ↓
Create delivery/work item per subscriber
```

Pros:

- Fast consumer reads
- Easy delivery tracking

Cons:

- Huge write amplification for large audiences

## Fan-Out on Read

Store the message once:

```text
Topic → Messages
```

Subscribers read messages when consuming.

Pros:

- Lower write amplification

Cons:

- More work during reads
- Real-time behavior can be more complex

### Interview Answer

> "For smaller subscriber counts, fan-out on write is simple. At very large scale, I'd consider partitioned event streams and choose fan-out strategy based on read/write load and latency requirements."

---

# 32. Backpressure

If publishers produce messages faster than notification workers can process them:

```text
Producer rate
     >
Consumer rate
```

the system needs backpressure.

Possible tools:

```text
bounded queues
broker retention
rate limiting
consumer scaling
batching
```

Never assume unlimited asynchronous processing is safe.

---

# 33. Ordering

Another likely follow-up:

> "Do subscribers receive messages in order?"

The current design does not guarantee ordering.

If ordering matters, introduce:

```text
sequence number
partition key
topic partition
subscriber offset
```

For example:

```text
M1
M2
M3
```

must not become:

```text
M2
M1
M3
```

unless the business requirement permits it.

---

# 34. Delivery Semantics

Production messaging systems usually choose between:

```text
At-most-once
At-least-once
Exactly-once
```

### At-most-once

No duplicate delivery, but messages can be lost.

### At-least-once

Messages can be retried, so duplicates are possible.

### Exactly-once

Much harder to guarantee end-to-end.

A common practical choice is:

```text
At-least-once
+
Idempotent consumers
```

---

# 35. Multi-Instance Scaling

Suppose there are multiple application instances:

```text
Instance 1
Instance 2
Instance 3
...
```

A subscriber may have their WebSocket connection on Instance 2, while a publisher's request reaches Instance 1.

Instance 1 cannot rely on local memory to find the connection.

A production architecture needs something like:

```text
Publisher
   ↓
Broker
   ↓
Realtime Service
   ↓
Connection Registry
   ↓
Target Instance
   ↓
WebSocket
```

This is a major limitation of process-local observer lists.

---

# 36. Connection Registry

The current design stores:

```text
realtimeConnectionId
```

A distributed system may maintain:

```text
subscriberId → active connections
```

in a shared registry.

Potential technologies depend on requirements:

```text
Redis
database
service discovery / gateway state
```

---

# 37. Heartbeats

The model stores:

```text
lastHeartbeat
```

This can help detect stale connections.

Conceptually:

```text
currentTime - lastHeartbeat > threshold
          ↓
mark offline
```

This prevents messages from being sent to dead connections.

---

# 38. Security

A production system must authenticate and authorize users.

Questions include:

```text
Who can create a topic?
Who can publish?
Who can subscribe?
Who can deactivate a topic?
Who can acknowledge a delivery?
```

Spring Security can handle API-level authentication/authorization.

---

# 39. Validation

Examples:

```text
topic must exist
topic must be active
subscriber must exist
email must be valid
duplicate subscription should be prevented
message content must satisfy limits
```

A useful split:

```text
Controller
   ↓
Request validation

Service
   ↓
Business validation

Domain
   ↓
Domain invariants
```

---

# 40. DTOs

The current controllers use simple parameters such as:

```java
@RequestParam String content
```

For production, request DTOs are often cleaner:

```text
PublishMessageRequest
RegisterSubscriberRequest
SubscribeRequest
AcknowledgeMessageRequest
```

Advantages:

- Validation
- Strong typing
- Cleaner API contracts
- Easier API evolution

---

# 41. Exception Handling

The current implementation uses generic exceptions such as:

```java
RuntimeException
```

A production version could introduce:

```text
TopicNotFoundException
SubscriberNotFoundException
InactiveTopicException
SubscriptionAlreadyExistsException
MessageDeliveryNotFoundException
```

and centrally map them with:

```java
@RestControllerAdvice
```

---

# 42. Why Subscription Is an Entity

Instead of:

```text
Map<Topic, List<Subscriber>>
```

an explicit `Subscription` entity lets us store metadata:

```text
active
createdAt
channel preferences
filters
delivery settings
```

It also makes queries and persistence much easier.

---

# 43. Extensible Subscription Preferences

A future subscription could contain:

```text
emailEnabled
realtimeEnabled
minimumPriority
keywordFilter
```

For example:

```text
Subscriber A
 └── Technology
       ├── Email = ON
       ├── Realtime = ON
       └── Priority >= HIGH
```

This is a natural extension of the current model.

---

# 44. Production Notification Architecture

A scalable architecture could look like:

```text
                    Publisher
                       │
                       ▼
                Publisher Service
                       │
                 ┌─────┴─────┐
                 │           │
                 ▼           ▼
             Message DB   Outbox Event
                               │
                               ▼
                         Message Broker
                         /      |      \
                        /       |       \
                       ▼        ▼        ▼
                    Email    Realtime   Other
                    Worker    Worker   Consumer
                       │        │
                       ▼        ▼
                    Email     Connection
                    Provider  Registry
```

---

# 45. Testing Strategy

## TopicService

Test:

```text
create topic
list topics
deactivate topic
```

## PublisherService

Test:

```text
topic missing
topic inactive
message created
message persisted
async processing triggered
```

## SubscriptionService

Test:

```text
subscribe
duplicate subscription
unsubscribe
online subscriber gets realtime observer
offline subscriber does not
```

## SubscriberService

Test:

```text
register subscriber
go online
go offline
heartbeat update
pending delivery processing
```

## MessageService

Test:

```text
delivery exists
acknowledgement changes status
acknowledgedAt is populated
```

---

# 46. Observer Tests

A simple unit test should verify:

```text
MessageSubject
      ↓
EmailSubscriber
RealtimeSubscriber
```

When:

```java
notify(message)
```

is called, both observers receive:

```java
update(message)
```

---

# 47. Concurrency Test Cases

Important scenarios:

```text
Two concurrent subscriptions
Subscriber goes online while message is being published
Subscriber goes offline during delivery
Two acknowledgement requests arrive together
Multiple messages published concurrently
Delivery gets retried
```

---

# 48. State of the Current Implementation

Be honest about what is implemented versus simulated.

### Implemented conceptually

```text
Topic management
Subscriber management
Subscription relationships
Observer Pattern
Email / realtime observer types
Message persistence abstraction
Delivery persistence abstraction
Async publishing
Online/offline tracking
Acknowledgement model
```

### Still placeholder / TODO

```text
Actual email sending
Actual WebSocket delivery
Exact realtime observer removal
MessageDelivery creation during notification
MessageDelivery lookup for acknowledgement
Pending delivery replay
Complete search/validation logic
Durable persistence
Retries
DLQ
Distributed connection management
```

Do not claim the TODO functionality is production-ready.

---

# 49. One-Minute Interview Explanation

A strong answer to "Explain your design":

> "I designed a publish/subscribe system where publishers publish messages to topics and subscribers subscribe to those topics. The core design pattern is Observer: each topic has a message subject and notification mechanisms such as email and real-time implement a common subscriber interface. I separated the system into domain, repository, service, and controller layers. Messages and message deliveries are separate entities because one published message can result in multiple subscriber-specific delivery states and acknowledgements. Publishing is asynchronous in the current implementation so notification work doesn't block message creation. For production, I would replace the in-memory repositories and process-local observers with durable persistence and an event-driven architecture using a message broker, with retries, idempotency, dead-letter handling, and a distributed connection registry."

---

# 50. State the Trade-offs Clearly

Interviewers care about trade-offs.

### Observer Pattern

Pros:

```text
loose coupling
easy extension
simple LLD
```

Cons:

```text
in-process
harder distributed scaling
observer failures can affect notification flow
```

### CompletableFuture

Pros:

```text
simple
non-blocking at caller level
easy for demo
```

Cons:

```text
not durable
limited failure handling
limited backpressure
executor management concerns
```

### In-memory Repository

Pros:

```text
simple
fast
easy for testing
```

Cons:

```text
data loss on restart
not shared across instances
no durable transactions
```

---

# 51. Quick Comparison Table

| Concept | In This System |
|---|---|
| Pub/Sub abstraction | Topic + Subscription |
| Main pattern | Observer |
| Subject | MessageSubject |
| Observer interface | SubscriberObserver |
| Email observer | EmailSubscriber |
| Realtime observer | RealtimeSubscriber |
| Message storage | MessageRepository |
| Delivery tracking | MessageDeliveryRepository |
| Async processing | CompletableFuture |
| Online state | Subscriber.online |
| Connection tracking | realtimeConnectionId |
| Delivery lifecycle | PENDING → DELIVERED → ACKNOWLEDGED |
| Spring layer | Controller → Service → Repository |

---

# 52. Final 15 Things to Remember

```text
1. Topic = logical publication channel
2. Subscriber = recipient
3. Subscription = subscriber ↔ topic relationship
4. Message = published content
5. MessageDelivery = subscriber-specific delivery record
6. Observer Pattern = multiple notification consumers
7. MessageSubject = Observer subject
8. SubscriberObserver = Observer contract
9. PublisherService = publishing workflow
10. SubscriptionService = subscription workflow
11. SubscriberService = online/offline lifecycle
12. MessageService = acknowledgement workflow
13. Repository = persistence abstraction
14. CompletableFuture = demo-level async processing
15. Production = broker + durable storage + idempotency + retries
```

---

# 53. Last-Minute Mental Model

Remember this:

```text
                  TOPIC
                    │
                    ▼
              MESSAGE SUBJECT
               /           \
              /             \
             ▼               ▼
          EMAIL           REALTIME
         OBSERVER          OBSERVER


Publisher
   ↓
Message
   ↓
Async Delivery
   ↓
Subscribers
   ↓
MessageDelivery
   ↓
ACKNOWLEDGED
```

And the application:

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Persistence
```

For production:

```text
Service
  ↓
Outbox
  ↓
Message Broker
  ↓
Consumers
  ├── Email
  ├── Realtime
  └── Other
```

---

# 54. Ideal Interview Mindset

Don't just explain what the code does.

Explain:

```text
Requirement
    ↓
Design Decision
    ↓
Pattern / Abstraction
    ↓
Trade-off
    ↓
Production Evolution
```

Example:

> "A message may need to notify many different channels, so I used Observer to decouple the publisher from delivery implementations. For a production distributed system, I would move that in-process observer notification to a durable event broker and make the consumers idempotent."

That demonstrates LLD and distributed-systems thinking instead of simply describing classes.
