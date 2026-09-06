# Ride Sharing LLD — Interview Revision Notes

A quick-revision guide for the Ride Sharing Low-Level Design implemented in Java/Spring Boot.

---

## 1. Problem Statement

Design a ride-sharing system that supports:

- Rider registration / lookup
- Driver availability management
- Driver location updates
- Fare estimation
- Ride requests
- Driver matching
- Driver accept / decline
- Ride lifecycle management
- Payment handling
- Notifications
- Ride status tracking
- Cancellation
- In-memory repositories for the LLD implementation

The implementation is structured so that the domain logic is separated from repositories, services, strategies, state management, and controllers.

---

# 2. High-Level Architecture

```text
                    ┌─────────────────────┐
                    │      REST API       │
                    │ Controllers         │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │      Services       │
                    │                     │
                    │ RideService         │
                    │ DriverService       │
                    │ MatchingService     │
                    │ PricingService      │
                    │ PaymentService      │
                    │ LocationService     │
                    │ NotificationService │
                    └───────┬─────┬───────┘
                            │     │
                 ┌──────────┘     └──────────┐
                 ▼                           ▼
       ┌──────────────────┐        ┌──────────────────┐
       │ Strategies       │        │ State Machine    │
       │                  │        │                  │
       │ Pricing          │        │ RideStateMachine │
       │ Driver Matching  │        └──────────────────┘
       └──────────────────┘
                 │
                 ▼
       ┌─────────────────────┐
       │ Repository Layer    │
       │                     │
       │ RideRepository      │
       │ DriverRepository    │
       │ RiderRepository     │
       │ LocationRepository  │
       └──────────┬──────────┘
                  ▼
       ┌─────────────────────┐
       │ In-Memory Storage   │
       │ ConcurrentHashMap   │
       └─────────────────────┘
```

### Main principle

Controllers should be thin.

```text
Controller
    ↓
Service
    ↓
Repository / Strategy / State Machine
```

Business rules should primarily live in services rather than controllers.

---

# 3. Core Domain Objects

## Rider

Represents the customer requesting a ride.

Important fields:

```text
id
name
email
phone
createdAt
```

---

## Driver

Represents a driver who can accept rides.

Important fields:

```text
id
name
email
phone
vehicleNumber
vehicleType
status
currentLocation
lastLocationUpdate
```

### DriverStatus

```text
OFFLINE
ONLINE
ON_RIDE
```

Typical lifecycle:

```text
OFFLINE → ONLINE → ON_RIDE → ONLINE
```

---

## Location

```text
latitude
longitude
address
timestamp
```

Used for:

- Driver location tracking
- Distance calculation
- Driver matching
- Ride pickup/drop-off information

---

## Ride

Important fields:

```text
rideId
riderId
driverId
pickup
dropoff
status
paymentStatus
paymentType
fare
distanceKm
durationSec
createdAt
updatedAt
completedAt
cancellationReason
```

---

## RideRequest

Represents the incoming ride request.

```text
riderId
pickup
dropoff
paymentType
```

---

# 4. Ride Lifecycle

The most important thing to remember for an interview is the state machine.

```text
             ┌───────────────┐
             │   REQUESTED   │
             └───────┬───────┘
                     │
                     ▼
             ┌───────────────┐
             │   ASSIGNED    │
             └───────┬───────┘
                     │
                     ▼
             ┌───────────────┐
             │   ACCEPTED    │
             └───────┬───────┘
                     │
                     ▼
             ┌───────────────┐
             │  IN_PROGRESS  │
             └───────┬───────┘
                     │
                     ▼
             ┌───────────────┐
             │   COMPLETED   │
             └───────────────┘
```

Cancellation can happen from several intermediate states:

```text
REQUESTED ──────→ CANCELLED
ASSIGNED  ──────→ CANCELLED
ACCEPTED  ──────→ CANCELLED
IN_PROGRESS ────→ CANCELLED
```

Other supported status:

```text
DENIED
```

### State Machine

The `RideStateMachine` uses an `EnumMap<RideStatus, Set<RideStatus>>`.

Conceptually:

```text
REQUESTED:
    ASSIGNED
    CANCELLED

ASSIGNED:
    ACCEPTED
    CANCELLED
    REQUESTED

ACCEPTED:
    IN_PROGRESS
    CANCELLED

IN_PROGRESS:
    COMPLETED
    CANCELLED

COMPLETED:
    no transitions

CANCELLED:
    no transitions
```

### Interview point

Do NOT scatter checks like this everywhere:

```java
if (status == REQUESTED) ...
else if (status == ACCEPTED) ...
```

A state machine centralizes valid transitions.

This makes invalid transitions easier to prevent and maintain.

---

# 5. Main Ride Flow

## Step 1 — Rider requests a ride

```text
Rider
  ↓
RideController
  ↓
RideService.requestRide()
  ↓
Validate rider
  ↓
Calculate fare
  ↓
Create Ride
  ↓
Save Ride
  ↓
Payment handling
  ↓
Driver matching
```

The ride initially starts as:

```text
REQUESTED
```

---

# 6. Driver Matching

The system uses a strategy:

```java
DriverMatchingStrategy
```

Current implementation:

```java
NearestDriverStrategy
```

### Why Strategy Pattern?

Because the matching algorithm can change.

Today:

```text
Nearest driver
```

Tomorrow:

```text
Highest rated driver
Cheapest driver
Preferred vehicle
Driver with shortest ETA
Driver with best acceptance probability
```

The service should not need to change every time the algorithm changes.

---

# 7. Nearest Driver Strategy

The implementation calculates an approximate geographic distance using latitude/longitude.

Conceptually:

```text
distance ≈ Euclidean distance × 111 km
```

This is sufficient for an LLD demonstration.

A real production implementation would use:

- Haversine distance
- Geospatial indexes
- Redis GEO
- Elasticsearch/OpenSearch geo queries
- Dedicated location services

### Interview answer

If asked:

> "How would you find the nearest driver at scale?"

Say:

> "I would avoid scanning every driver. I would maintain driver locations in a geospatial data store such as Redis GEO and query nearby available drivers using a radius or nearest-neighbor query."

---

# 8. Driver Matching Flow

```text
Ride REQUESTED
      ↓
Find ONLINE drivers
      ↓
NearestDriverStrategy
      ↓
Get top candidates
      ↓
Acquire driver lock
      ↓
Re-check driver availability
      ↓
Notify driver
      ↓
Wait for response
      ↓
Accepted?
   /       \
 YES       NO
  ↓         ↓
Assign    Try next
driver    candidate
```

### Important concurrency idea

Finding an available driver and assigning that driver are NOT one atomic operation.

Example:

```text
Thread A → sees Driver 101 as ONLINE
Thread B → sees Driver 101 as ONLINE
Thread A → assigns Driver 101
Thread B → also tries to assign Driver 101
```

This can cause double assignment.

The implementation therefore uses a lock around driver assignment.

---

# 9. LockService

The LLD uses:

```java
ConcurrentHashMap<String, ReentrantLock>
```

Locks are keyed by a resource ID.

For example:

```text
driver:101
driver:102
```

The service supports:

```text
acquire()
release()
```

with a timeout.

### Why tryLock?

Instead of blocking forever:

```java
lock.lock();
```

we use a bounded attempt:

```java
lock.tryLock(timeout)
```

This prevents one request from waiting indefinitely for another thread.

### Production improvement

A local `ReentrantLock` only works inside one JVM.

For multiple application instances:

```text
Instance A
Instance B
Instance C
```

a local lock cannot protect shared state.

A production design could use:

- Redis distributed locks
- Database row locking
- Optimistic locking
- Atomic state transitions

---

# 10. Pricing

Pricing is also implemented using Strategy Pattern.

```java
PricingStrategy
        ↓
BasePricingStrategy
```

Current conceptual formula:

```text
Fare =
max(
    baseFare,
    baseFare
      + distanceKm × perKmRate
      + durationMin × perMinuteRate
)
```

Current configured values in the implementation are represented in minor currency units.

### Why Strategy Pattern?

Future strategies:

```text
Base pricing
Surge pricing
Peak-hour pricing
Premium vehicle pricing
Coupon pricing
Dynamic pricing
```

Could become:

```text
BasePricingStrategy
SurgePricingStrategy
PremiumPricingStrategy
```

without changing the caller.

---

# 11. Fare Estimation

Fare estimation uses:

```text
pickup
dropoff
      ↓
LocationService
      ↓
distance calculation
      ↓
duration estimation
      ↓
PricingService
      ↓
PricingStrategy
      ↓
FareEstimateResponse
```

The response contains:

```text
estimatedFare
distanceKm
durationSec
currency
```

### Production improvement

Instead of estimating duration purely from:

```text
distance / average speed
```

use a routing provider such as a maps/routing service that understands:

- traffic
- road network
- turn restrictions
- live ETA

---

# 12. Location Service

Responsibilities:

- Update driver location
- Fetch latest driver location
- Calculate distance
- Estimate duration

Current implementation stores the latest location through `LocationRepository`.

### Important design principle

Location management is kept separate from `DriverService` because location can become a high-volume subsystem.

At scale, location updates may arrive every few seconds from thousands/millions of drivers.

A production architecture might use:

```text
Driver App
    ↓
Location API
    ↓
Kafka / Kinesis
    ↓
Location Processing
    ↓
Redis GEO / Location Store
```

---

# 13. Payment

Payment concepts:

```text
PaymentType
```

```text
PRE_PAYMENT
POST_PAYMENT
```

and:

```text
PaymentStatus
```

```text
NONE
PENDING
COMPLETED
FAILED
REFUNDED
```

---

## Pre-payment flow

```text
Ride request
    ↓
Create payment
    ↓
Payment PENDING
    ↓
External gateway
    ↓
Callback
    ↓
COMPLETED
    ↓
Start matching
```

---

## Post-payment flow

```text
Ride request
    ↓
Ride matching
    ↓
Driver accepts
    ↓
Ride starts
    ↓
Ride completes
    ↓
Payment initiated/completed
```

The exact implementation should be explained according to the code rather than assuming a specific external payment provider.

---

# 14. Payment Idempotency

The implementation maintains a mapping:

```text
rideId → paymentId
```

This helps avoid blindly creating multiple payment IDs for the same ride.

### Interview question

> "What happens if the payment provider sends the callback twice?"

Answer:

> "The callback endpoint must be idempotent. We should identify the payment/ride using a unique payment reference and make processing safe to repeat. In production, I would persist the payment state with a unique constraint and use an atomic state transition."

---

# 15. Payment Callback

Typical flow:

```text
Payment Provider
       ↓
POST /payment/callback
       ↓
PaymentController
       ↓
PaymentService
       ↓
Validate callback
       ↓
Update payment status
       ↓
Update ride / continue workflow
```

### Production concerns

A real payment callback should include:

- Signature verification
- Idempotency key
- Authentication
- Replay protection
- Persistent payment record
- Audit trail
- Retry handling
- Dead-letter handling

---

# 16. Notification Service

The current LLD logs notifications instead of integrating a real provider.

Conceptually:

```text
NotificationService
        ↓
NotificationMessage
        ↓
send notification
```

Notifications could eventually be sent using:

```text
Push notification
SMS
Email
WebSocket
```

### Important principle

Ride logic should not depend directly on a specific notification vendor.

Prefer:

```text
RideService
    ↓
NotificationService
    ↓
Push/SMS/Email implementation
```

This keeps the domain logic decoupled.

---

# 17. Repository Pattern

Repositories abstract persistence.

Examples:

```text
DriverRepository
RiderRepository
RideRepository
LocationRepository
```

The service layer should not directly manipulate:

```java
ConcurrentHashMap
```

Instead:

```text
Service
   ↓
Repository interface
   ↓
In-memory implementation
```

### Why?

The implementation can later change from:

```text
InMemoryRideRepository
```

to:

```text
JpaRideRepository
```

or:

```text
DynamoDBRideRepository
```

without rewriting the business service.

---

# 18. In-Memory Storage

The LLD uses:

```java
ConcurrentHashMap
```

This is appropriate for a simple single-process LLD.

Benefits:

- Thread-safe map operations
- Fast lookup
- Easy setup
- No external database dependency

But it is NOT durable.

If the application restarts:

```text
All rides/drivers disappear
```

### Production alternative

```text
PostgreSQL / MySQL
DynamoDB
MongoDB
Redis
```

depending on the data and access pattern.

---

# 19. Driver Lifecycle

Driver can be:

```text
OFFLINE
ONLINE
ON_RIDE
```

Typical flow:

```text
OFFLINE
   ↓
ONLINE
   ↓
ON_RIDE
   ↓
ONLINE
```

When a driver accepts a ride:

```text
ONLINE → ON_RIDE
```

When the ride completes:

```text
ON_RIDE → ONLINE
```

When driver goes offline:

```text
ONLINE → OFFLINE
```

### Important interview point

Driver availability should be revalidated immediately before assignment.

Do not trust an earlier search result.

---

# 20. Ride Cancellation

Cancellation should:

1. Validate the ride
2. Validate whether cancellation is allowed
3. Update ride status
4. Store cancellation reason
5. Release driver if necessary
6. Handle payment/refund implications
7. Notify relevant parties

Conceptually:

```text
Ride
 ↓
CANCELLED
 ↓
Release driver
 ↓
Payment/refund handling
 ↓
Notification
```

---

# 21. Thread Safety

Potential race conditions:

### Example 1 — Two drivers accept

```text
Driver A → accepts
Driver B → accepts
```

Only one should win.

Need atomic assignment / locking.

### Example 2 — Two requests use same driver

```text
Ride 1 → Driver 101
Ride 2 → Driver 101
```

Driver availability must be checked atomically.

### Example 3 — Two payment callbacks

```text
Callback 1 → COMPLETED
Callback 2 → COMPLETED
```

Processing should be idempotent.

### Example 4 — Cancel + accept race

```text
Thread A → rider cancels
Thread B → driver accepts
```

Need atomic state transitions.

---

# 22. SOLID Principles Used

## Single Responsibility Principle

Different services have focused responsibilities:

```text
RideService → ride lifecycle
PaymentService → payment
MatchingService → driver matching
PricingService → pricing
LocationService → location
NotificationService → notification
```

---

## Open/Closed Principle

Strategies allow adding algorithms without modifying existing callers.

```text
PricingStrategy
DriverMatchingStrategy
```

---

## Liskov Substitution Principle

Any valid implementation of:

```java
PricingStrategy
```

should be usable where the interface is expected.

Same for:

```java
DriverMatchingStrategy
```

---

## Interface Segregation

Small repository/strategy interfaces are preferable to one giant interface.

---

## Dependency Inversion

Services depend on abstractions:

```text
RideService
    ↓
RideRepository
```

rather than directly depending on:

```text
ConcurrentHashMap
```

Spring dependency injection handles the wiring.

---

# 23. Design Patterns

## Strategy Pattern

Used for:

```text
Pricing
Driver Matching
```

Use when behavior/algorithm can vary.

---

## State Pattern / State Machine

Used for:

```text
Ride lifecycle
```

Use when an entity's valid operations depend heavily on its current state.

---

## Repository Pattern

Used for:

```text
Persistence abstraction
```

---

## Dependency Injection

Spring provides dependencies instead of services manually constructing them.

---

# 24. Spring Boot Structure

Main application:

```java
@SpringBootApplication
public class RideSharingApplication {
}
```

Typical annotations:

```text
@Service
@Repository
@RestController
@Component
@RequiredArgsConstructor
@Slf4j
```

### What they mean

`@Service`

Marks business/service layer.

`@Repository`

Marks persistence layer.

`@RestController`

Exposes REST APIs.

`@Component`

Generic Spring-managed bean.

`@RequiredArgsConstructor`

Lombok generates constructor injection for final fields.

`@Slf4j`

Provides structured logging through SLF4J.

---

# 25. Why Constructor Injection?

Prefer:

```java
@RequiredArgsConstructor
@Service
public class RideService {

    private final RideRepository rideRepository;
}
```

over field injection:

```java
@Autowired
private RideRepository rideRepository;
```

Benefits:

- Dependencies are explicit
- Easier unit testing
- Supports immutable fields
- Avoids partially initialized objects
- Better design for mandatory dependencies

---

# 26. REST API Responsibilities

### RideController

Handles:

```text
Fare estimate
Request ride
Get ride status
Cancel ride
```

### DriverController

Handles:

```text
Go online
Go offline
Update location
Accept ride
Decline ride
Start ride
Complete ride
```

### PaymentController

Handles:

```text
Payment callback
```

Controllers should primarily:

```text
receive request
    ↓
validate basic input
    ↓
call service
    ↓
return response
```

Avoid putting complicated business logic in controllers.

---

# 27. Important Interview Questions

## Q1. Why use a strategy for driver matching?

Because matching is an interchangeable algorithm.

Example:

```text
Nearest driver
        ↓
Highest rated driver
        ↓
Best ETA
        ↓
Preferred vehicle
```

The service remains unchanged.

---

## Q2. How do you prevent two rides from getting the same driver?

Use:

- Locking
- Atomic state transition
- Optimistic locking
- Database conditional update
- Distributed lock in a multi-instance deployment

---

## Q3. Why isn't ReentrantLock enough in production?

Because it only protects threads inside the same JVM.

With multiple instances:

```text
Instance A → local lock
Instance B → different local lock
```

They do not coordinate.

Use a distributed coordination mechanism.

---

## Q4. How would you scale driver matching?

Do not scan every driver.

Use:

```text
Geospatial index
+
available-driver index
+
candidate ranking
```

Example:

```text
Redis GEO
```

---

## Q5. How would you handle millions of location updates?

Do not synchronously write every update into the primary database.

Possible architecture:

```text
Driver
  ↓
Location API
  ↓
Kafka/Kinesis
  ↓
Location processor
  ↓
Redis GEO / fast location store
```

Persist historical location asynchronously if required.

---

## Q6. What if a driver doesn't respond?

Use:

```text
timeout
    ↓
mark candidate unavailable for this request
    ↓
try next driver
```

Do not block indefinitely.

---

## Q7. What if driver accepts at exactly the same time as rider cancels?

The system needs a defined winner.

Use an atomic state transition such as:

```text
REQUESTED → ASSIGNED
```

or:

```text
REQUESTED → CANCELLED
```

Only one transaction should succeed.

---

## Q8. What if payment callback is duplicated?

Make callback processing idempotent.

Use:

```text
paymentId / transactionId
+
unique constraint
+
atomic status transition
```

---

## Q9. What if the application crashes after assigning the driver?

Persist the assignment in durable storage.

On restart, recover:

```text
REQUESTED
ASSIGNED
ACCEPTED
IN_PROGRESS
```

rides that are still active.

---

## Q10. Why use repositories?

To isolate persistence from business logic.

This allows:

```text
InMemoryRepository
```

to be replaced by:

```text
JPA / DynamoDB / MongoDB
```

without changing the service layer.

---

# 28. Important Edge Cases

Be ready to mention these:

### Rider

- Rider doesn't exist
- Invalid pickup/dropoff
- Same pickup and dropoff
- Rider already has an active ride

### Driver

- Driver offline
- Driver already on another ride
- Driver location unavailable
- Driver accepts after timeout
- Driver accepts a cancelled ride

### Ride

- Invalid state transition
- Ride already completed
- Ride already cancelled
- Cancellation after completion

### Payment

- Duplicate callback
- Payment failure
- Payment timeout
- Refund
- Callback received after ride cancellation

### Concurrency

- Two drivers accept
- Two rides assign same driver
- Cancel and accept happen simultaneously
- Multiple matching attempts run concurrently

---

# 29. Production Improvements

The current implementation is intentionally an LLD / in-memory design.

For production:

## Persistence

Replace:

```text
ConcurrentHashMap
```

with durable storage.

---

## Distributed locking

Replace:

```text
ReentrantLock
```

with:

```text
Redis lock
DB locking
optimistic concurrency
```

depending on the use case.

---

## Driver location

Use:

```text
Redis GEO
```

or another geospatial system.

---

## Async processing

Potentially use:

```text
Kafka
AWS Kinesis
SQS
RabbitMQ
```

for:

- Location events
- Notifications
- Payment events
- Analytics
- Audit events

---

## Caching

Use Redis for:

```text
Driver availability
Latest driver location
Frequently accessed ride status
```

---

## Observability

Add:

```text
Metrics
Distributed tracing
Structured logs
Correlation IDs
Alerts
Dashboards
```

Useful metrics:

```text
ride_request_success_rate
ride_assignment_time
driver_acceptance_rate
payment_success_rate
ride_cancellation_rate
matching_latency
```

---

# 30. One Important Design Limitation

The current matching implementation waits/polls for a driver response.

Conceptually:

```text
Request thread
    ↓
Notify driver
    ↓
Wait
    ↓
Poll ride status
    ↓
Timeout
```

This is acceptable for demonstrating the LLD flow, but it is not ideal production architecture.

A better design would be asynchronous:

```text
Ride Request
    ↓
Create Ride
    ↓
Publish Matching Event
    ↓
Matching Worker
    ↓
Notify Driver
    ↓
Driver accepts/declines asynchronously
    ↓
Ride state updated
```

This avoids keeping an HTTP request thread blocked while waiting for a driver.

---

# 31. Another Important Concurrency Bug to Watch For

When reviewing the matching loop, be careful with conditions involving timeout and terminal states.

A condition conceptually equivalent to:

```java
while (notTimedOut || status == DENIED)
```

can continue unexpectedly if the status remains `DENIED`.

The safer approach is to explicitly handle terminal states and timeout:

```text
while (notTimedOut) {
    if (accepted) return;
    if (cancelled) return;
    if (denied) break;
}
```

In an interview, explicitly calling out this kind of condition is a strong sign of concurrency/debugging awareness.

---

# 32. 60-Second Interview Explanation

If the interviewer says:

> "Explain your ride-sharing design."

You can answer:

> "I designed the system around a service-oriented architecture with separate services for ride lifecycle, driver management, matching, pricing, payments, location, and notifications. The ride lifecycle is controlled through a state machine so only valid transitions such as REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED are allowed. For driver matching and pricing, I used the Strategy Pattern so algorithms can be changed independently. Persistence is abstracted behind repository interfaces, with in-memory implementations for the LLD. Since driver assignment is a concurrency-sensitive operation, I use locking and re-check driver availability before assignment. Payment processing supports pre- and post-payment flows, and callbacks should be idempotent. For production, I would replace in-memory storage with durable databases, use distributed locking or atomic DB operations, maintain driver locations using a geospatial store such as Redis GEO, and make driver matching asynchronous using an event-driven architecture."

---

# 33. Whiteboard Order

When asked to design this from scratch, follow this order:

```text
1. Requirements
       ↓
2. Core entities
       ↓
3. Enums / states
       ↓
4. State transitions
       ↓
5. Repository interfaces
       ↓
6. Services
       ↓
7. Strategy patterns
       ↓
8. Concurrency
       ↓
9. APIs
       ↓
10. Production scaling
```

Don't start by writing classes randomly.

---

# 34. Core Class Cheat Sheet

```text
Ride
 ├── riderId
 ├── driverId
 ├── pickup
 ├── dropoff
 ├── status
 ├── paymentStatus
 └── paymentType

Driver
 ├── id
 ├── status
 ├── vehicle
 └── location

Rider
 └── profile information

RideService
 └── ride lifecycle

DriverService
 └── driver availability/location

MatchingService
 └── driver assignment

PricingService
 └── fare calculation

PaymentService
 └── payment lifecycle

LocationService
 └── location/distance/ETA

NotificationService
 └── notifications

RideStateMachine
 └── valid ride transitions

PricingStrategy
 └── pricing algorithm

DriverMatchingStrategy
 └── matching algorithm

Repositories
 └── persistence abstraction
```

---

# 35. Final Revision Checklist

Before an interview, make sure you can explain:

- [ ] Ride lifecycle
- [ ] State machine
- [ ] Driver lifecycle
- [ ] Driver matching
- [ ] Why Strategy Pattern?
- [ ] Why Repository Pattern?
- [ ] How driver assignment is synchronized
- [ ] Why local locks don't work across instances
- [ ] Fare calculation
- [ ] Pre-payment vs post-payment
- [ ] Payment idempotency
- [ ] Location management
- [ ] REST API responsibilities
- [ ] SOLID principles
- [ ] Thread-safety concerns
- [ ] Race conditions
- [ ] Scaling driver matching
- [ ] Scaling location updates
- [ ] Async/event-driven architecture
- [ ] Production persistence
- [ ] Observability
- [ ] Failure scenarios

---

# 36. The Five Things You Absolutely Must Remember

If you have only **2 minutes before the interview**, remember these:

### 1. State Machine

```text
REQUESTED
 → ASSIGNED
 → ACCEPTED
 → IN_PROGRESS
 → COMPLETED
```

Cancellation can happen from intermediate states.

### 2. Strategy Pattern

```text
PricingStrategy
DriverMatchingStrategy
```

Used to make algorithms replaceable.

### 3. Concurrency

Driver selection is not enough.

You must:

```text
find candidate
    ↓
lock / atomically reserve
    ↓
re-check availability
    ↓
assign
```

### 4. Scale

For production:

```text
Redis GEO → nearby drivers
Distributed coordination → driver assignment
Kafka/Kinesis → async events
Database → durable state
```

### 5. Async Matching

Don't keep an HTTP request waiting for a driver.

Prefer:

```text
Ride Request
   ↓
Event
   ↓
Matching
   ↓
Driver response
   ↓
State update
```

---

## Final Interview Mental Model

Think of the system as five independent problems:

```text
             RIDE
              │
      ┌───────┼────────┐
      ↓       ↓        ↓
   MATCHING  PRICING  PAYMENT
      │       │        │
      └───────┼────────┘
              ↓
           LOCATION
              │
              ↓
        STATE MACHINE
```

If you can clearly explain **state transitions + matching + concurrency + strategy patterns + production scaling**, you can handle most follow-up questions on this LLD.
