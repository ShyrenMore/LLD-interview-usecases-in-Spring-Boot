# Low-Level Design Prep

A hands-on collection of **Low-Level Design (LLD) / Object-Oriented
Design** problems implemented in Spring Boot and Lombok and structured for interview
preparation.

The repository focuses on designing maintainable, extensible and
testable systems using **SOLID principles, design patterns, clean
separation of responsibilities, concurrency concepts, and practical
domain modeling**.

## Repository

urlGitHub
Repositoryhttps://github.com/ShyrenMore/low-level-design-prep

------------------------------------------------------------------------

# Projects

  ---------------------------------------------------------------------------
  Project                                 Key Concepts
  --------------------------------------- -----------------------------------
  [ATM Machine](./atm-machine)            State pattern, cash dispensing,
                                          account operations, ATM states

  [Digital Wallet](./digital-wallet)      Strategy, Factory, concurrency,
                                          transactions, idempotency

  [Elevator System](./elevator-system)    Scheduling, state management,
                                          request handling

  [Hotel Management](./hotel-management)  Booking, room management,
                                          availability, reservations

  [Logging                                Chain of Responsibility, log
  Framework](./logging-framework)         levels, appenders, formatting

  [Music Streaming                        Playback state, Strategy, caching,
  Platform](./music-streaming-platform)   streaming, playlists

  [Parking Lot](./parking-lot)            Strategy, parking allocation,
                                          vehicle/spot modeling

  [Pub/Sub](./pub-sub)                    Observer-style messaging,
                                          publishers, subscribers, topics

  [Ride Booking](./ride-booking)          State, Strategy, matching,
                                          notifications, concurrency

  [Task Management](./task-management)    Task lifecycle, assignment,
                                          filtering, extensibility

  [Traffic Signal                         State pattern, signal transitions,
  System](./traffic-signal-system)        scheduling

  [Vending Machine](./vending-machine)    State pattern, inventory, payments,
                                          change calculation
  ---------------------------------------------------------------------------

------------------------------------------------------------------------

# What This Repository Demonstrates

## 1. Object-Oriented Design

The implementations emphasize:

-   Encapsulation
-   Abstraction
-   Inheritance where appropriate
-   Polymorphism
-   Composition over unnecessary inheritance
-   Interface-driven design

The goal is to model **behavior**, not just create a collection of data
classes.

------------------------------------------------------------------------

# 2. SOLID Principles

### Single Responsibility Principle

Classes should have one clear reason to change.

Example:

``` text
Controller
    ↓
Service
    ↓
Repository
```

rather than putting all business logic into one class.

### Open/Closed Principle

Systems should be open for extension but closed for modification.

Typical examples:

``` text
PaymentStrategy
PricingStrategy
RecommendationStrategy
MatchingStrategy
```

New behavior can be introduced by adding implementations instead of
modifying existing logic.

### Liskov Substitution Principle

Implementations of an abstraction should be usable wherever that
abstraction is expected.

### Interface Segregation Principle

Prefer focused interfaces over large interfaces containing unrelated
operations.

### Dependency Inversion Principle

High-level business logic should depend on abstractions rather than
concrete implementations.

------------------------------------------------------------------------

# 3. Design Patterns

The repository intentionally uses common interview patterns where they
naturally fit.

## Strategy Pattern

Use when an algorithm or business rule can vary.

``` text
Service
   ↓
Strategy
   ├── Implementation A
   ├── Implementation B
   └── Implementation C
```

Examples:

-   Recommendation strategies
-   Matching strategies
-   Pricing/allocation strategies

------------------------------------------------------------------------

## State Pattern

Use when an object's behavior changes based on its current state.

``` text
Object
  ↓
Current State
  ├── State A
  ├── State B
  └── State C
```

Examples:

-   ATM
-   Vending machine
-   Traffic signal
-   Ride lifecycle
-   Playback lifecycle

------------------------------------------------------------------------

## Factory Pattern

Use when object creation involves choosing between different
implementations/types.

Useful when:

``` text
Input
  ↓
Factory
  ↓
Correct implementation
```

------------------------------------------------------------------------

## Observer / Pub-Sub

Useful when one event needs to be delivered to multiple independent
consumers.

``` text
Publisher
    ↓
   Topic
  ↙ ↓ ↘
S1  S2  S3
```

------------------------------------------------------------------------

## Chain of Responsibility

Useful when a request should pass through a sequence of handlers.

A classic example is the logging framework:

``` text
DEBUG → INFO → WARN → ERROR
```

------------------------------------------------------------------------

# 4. Architecture Approach

Most projects follow a layered structure similar to:

``` text
             Client / Simulator
                     │
                     ▼
                Controller
                     │
                     ▼
                 Service
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
     Strategy / State       Repository
          │                     │
          ▼                     ▼
      Business Logic        Persistence
```

The exact structure varies by problem because LLD should be driven by
requirements rather than forcing every problem into the same
architecture.

------------------------------------------------------------------------

# 5. Project-Level Thinking

For each LLD problem, the recommended design process is:

### Step 1 --- Clarify Requirements

Separate:

``` text
Functional requirements
        +
Non-functional requirements
```

Ask:

-   What operations are required?
-   Who are the actors?
-   What are the important constraints?
-   What can change independently?
-   What concurrency is possible?

------------------------------------------------------------------------

### Step 2 --- Identify Core Entities

Start with nouns from the requirements.

Example:

``` text
Parking Lot
→ Vehicle
→ Parking Spot
→ Ticket
→ Floor
→ Payment
```

But do not blindly convert every noun into a class.

------------------------------------------------------------------------

### Step 3 --- Identify Behaviors

Ask:

> "Who should own this behavior?"

Avoid an anemic domain model where all behavior ends up inside one giant
service.

------------------------------------------------------------------------

### Step 4 --- Identify What Changes

This is often the key to selecting a design pattern.

Ask:

> "What part of this system is likely to change independently?"

If an algorithm changes:

``` text
Strategy
```

If behavior changes with state:

``` text
State
```

If object creation varies:

``` text
Factory
```

If events need multiple consumers:

``` text
Observer / Pub-Sub
```

------------------------------------------------------------------------

### Step 5 --- Define Interfaces

Introduce interfaces around genuine variation points.

Avoid creating interfaces just for the sake of having interfaces.

------------------------------------------------------------------------

### Step 6 --- Handle Edge Cases

Think about:

-   Invalid input
-   Missing entities
-   Empty collections
-   Duplicate operations
-   Concurrent requests
-   Resource exhaustion
-   State transition failures
-   Retry behavior
-   Authorization

------------------------------------------------------------------------

### Step 7 --- Discuss Production Evolution

A good LLD interview answer should explain how the in-memory design
would evolve.

``` text
In-memory repository
        ↓
Database
```

``` text
JVM-local cache
        ↓
Redis / distributed cache / CDN
```

``` text
Synchronous operation
        ↓
Event-driven asynchronous processing
```

------------------------------------------------------------------------

# 6. Concurrency

Several projects involve concurrent operations.

Important concepts to understand:

### Thread-safe collections

``` java
ConcurrentHashMap
```

can protect individual collection operations.

But:

> A thread-safe collection does not automatically make a multi-step
> business operation atomic.

For example:

``` text
read
→ modify
→ write
```

may still require synchronization or an atomic operation.

------------------------------------------------------------------------

## Locks

Possible tools:

``` text
synchronized
ReentrantLock
ReadWriteLock
```

Use explicit locks when the critical section and lock lifecycle need
more control.

Always release explicit locks in a `finally` block.

------------------------------------------------------------------------

## Distributed Concurrency

A JVM-local lock only protects one application instance.

With:

``` text
Instance A
Instance B
Instance C
```

a lock in A does not automatically protect state accessed by B.

Production alternatives include:

-   Optimistic locking
-   Database row/version locking
-   Distributed coordination
-   Redis-based locking where appropriate

------------------------------------------------------------------------

# 7. State Machines

A recurring concept across several projects is modeling an entity as a
state machine.

Example:

``` text
      ┌─────────┐
      │ CREATED │
      └────┬────┘
           │
           ▼
      ┌─────────┐
      │ ACTIVE  │
      └────┬────┘
           │
           ▼
      ┌─────────┐
      │  DONE   │
      └─────────┘
```

Before implementing a state-based system, explicitly identify:

1.  Valid states
2.  Valid transitions
3.  Events causing transitions
4.  Invalid transitions
5.  Behavior associated with each state

This is especially useful for:

-   ATM
-   Vending Machine
-   Ride Booking
-   Traffic Signals
-   Music Playback

------------------------------------------------------------------------

# 8. Repository Pattern

Repositories abstract persistence.

Instead of:

``` java
service → HashMap
```

prefer:

``` text
service
   ↓
Repository interface
   ↓
In-memory implementation
```

This allows the implementation to evolve:

``` text
InMemoryRepository
       ↓
DatabaseRepository
       ↓
Distributed storage
```

without rewriting business logic.

------------------------------------------------------------------------

# 9. Caching

When a system repeatedly accesses expensive data:

``` text
Request
   ↓
Cache
   ├── HIT → return
   └── MISS
        ↓
      Storage
        ↓
      Cache
        ↓
      Return
```

Always discuss:

-   Cache key
-   TTL
-   Eviction
-   Cache invalidation
-   Consistency
-   Maximum size
-   What happens when cache is unavailable

------------------------------------------------------------------------

# 10. Interview Preparation Strategy

For each project, be able to answer these questions without looking at
the code.

### Requirements

> What does the system need to do?

### Core Entities

> What are the important objects and relationships?

### Responsibilities

> Why does each class exist?

### Pattern

> Which design pattern did you use and why?

### Alternatives

> What other design could work?

### Extensibility

> How would you add a new requirement?

### Concurrency

> What happens if two requests modify the same object simultaneously?

### Persistence

> How would you replace the in-memory repository with a database?

### Scalability

> What changes when the application runs on multiple instances?

### Failure Handling

> What happens when a dependency or operation fails?

------------------------------------------------------------------------

# 11. High-Value Interview Questions

Before an interview, revise these concepts across the repository:

### OOP

-   Composition vs inheritance
-   Interface vs abstract class
-   Encapsulation
-   Polymorphism
-   Immutability

### SOLID

-   SRP
-   OCP
-   LSP
-   ISP
-   DIP

### Patterns

-   Strategy
-   State
-   Factory
-   Observer
-   Chain of Responsibility
-   Builder
-   Singleton --- and when **not** to use it

### Concurrency

-   synchronized
-   ReentrantLock
-   ConcurrentHashMap
-   Atomic operations
-   Race conditions
-   Deadlocks
-   Optimistic locking

### Architecture

-   Service layer
-   Repository layer
-   Dependency Injection
-   Event-driven architecture
-   Caching
-   Database persistence

------------------------------------------------------------------------

# 12. Recommended Interview Flow

When given a new LLD problem, use this sequence:

``` text
1. Clarify requirements
        ↓
2. Identify actors
        ↓
3. Identify core entities
        ↓
4. Define relationships
        ↓
5. Define important behaviors
        ↓
6. Identify variation points
        ↓
7. Select design patterns
        ↓
8. Define interfaces
        ↓
9. Implement core flow
        ↓
10. Handle edge cases
        ↓
11. Discuss concurrency
        ↓
12. Discuss extensibility
        ↓
13. Discuss production scaling
```

This prevents jumping directly into writing classes.

------------------------------------------------------------------------

# 13. What Good LLD Looks Like

A good design should be:

-   Easy to understand
-   Easy to extend
-   Easy to test
-   Loosely coupled
-   Highly cohesive
-   Explicit about responsibilities
-   Safe under expected concurrency
-   Reasonable to evolve into a production system

Avoid:

-   God classes
-   Huge `if/else` blocks for every future behavior
-   Excessive inheritance
-   Unnecessary interfaces
-   Global mutable state
-   Business logic inside controllers
-   Persistence logic inside domain/service classes
-   Premature abstraction

------------------------------------------------------------------------

# 14. Projects at a Glance

## ATM Machine

Focus on:

-   State transitions
-   Cash withdrawal
-   Authentication
-   Cash denomination handling
-   Account interaction

Key interview theme:

> State-dependent behavior.

------------------------------------------------------------------------

## Digital Wallet

Focus on:

-   Wallet/account modeling
-   Transactions
-   Transfer flow
-   Idempotency
-   Concurrency
-   Strategy/Factory-style extensibility

Key interview theme:

> Correctness of financial operations under concurrency and retries.

------------------------------------------------------------------------

## Elevator System

Focus on:

-   Elevator state
-   Floor requests
-   Request scheduling
-   Direction
-   Multiple elevators

Key interview theme:

> Scheduling and extensible allocation logic.

------------------------------------------------------------------------

## Hotel Management

Focus on:

-   Rooms
-   Reservations
-   Availability
-   Guests
-   Booking lifecycle

Key interview theme:

> Resource allocation and reservation consistency.

------------------------------------------------------------------------

## Logging Framework

Focus on:

-   Log levels
-   Handlers/appenders
-   Formatting
-   Multiple output destinations
-   Chain of Responsibility

Key interview theme:

> Extensible processing pipelines.

------------------------------------------------------------------------

## Music Streaming Platform

Focus on:

-   Playback sessions
-   Queues
-   Shuffle/repeat
-   Listening history
-   Streaming
-   Chunk cache
-   Recommendations
-   Playlists

Key interview theme:

> Combining state management, Strategy Pattern, caching, streaming and
> concurrency.

------------------------------------------------------------------------

## Parking Lot

Focus on:

-   Vehicle types
-   Spot allocation
-   Tickets
-   Pricing
-   Availability

Key interview theme:

> Allocation strategies and domain modeling.

------------------------------------------------------------------------

## Pub/Sub

Focus on:

-   Topics
-   Publishers
-   Subscribers
-   Message delivery
-   Decoupling

Key interview theme:

> Event-driven design and fan-out.

------------------------------------------------------------------------

## Ride Booking

Focus on:

-   Ride lifecycle
-   Driver matching
-   Acceptance/rejection
-   Pricing
-   Notifications
-   Concurrency

Key interview theme:

> State + Strategy + concurrent workflows.

------------------------------------------------------------------------

## Task Management

Focus on:

-   Tasks
-   Users
-   Assignment
-   Status
-   Priorities
-   Filtering

Key interview theme:

> Clean domain modeling and extensible business rules.

------------------------------------------------------------------------

## Traffic Signal System

Focus on:

-   Signal states
-   Transitions
-   Timing
-   Intersections

Key interview theme:

> State Machine design.

------------------------------------------------------------------------

## Vending Machine

Focus on:

-   Inventory
-   Payment
-   Change
-   Product selection
-   State transitions

Key interview theme:

> Classic State Pattern problem.

------------------------------------------------------------------------

# 15. Repository Philosophy

This repository is intended as a **practice ground**, not a collection
of production-ready distributed systems.

The implementations prioritize:

``` text
Clarity
   +
Object-oriented design
   +
Pattern usage
   +
Interview discussion
```

over introducing unnecessary infrastructure.

For interviews, the important skill is being able to explain:

> **Why this design? What changes if the requirements change? What
> breaks under concurrency? How would you evolve it for production?**

------------------------------------------------------------------------

# 16. Quick Revision Checklist

Before an LLD interview, make sure you can explain:

``` text
☐ Requirements
☐ Core entities
☐ Relationships
☐ Class responsibilities
☐ Interfaces
☐ SOLID principles
☐ Design patterns
☐ Main happy path
☐ Edge cases
☐ Error handling
☐ Concurrency
☐ Thread safety
☐ Persistence abstraction
☐ Caching
☐ Extensibility
☐ Scalability
☐ Production evolution
```

------------------------------------------------------------------------

# 17. Final Mental Model

When you see an LLD problem, think:

``` text
Requirements
     ↓
Entities
     ↓
Responsibilities
     ↓
Relationships
     ↓
Behavior
     ↓
What changes?
     ↓
Design Pattern
     ↓
Interfaces
     ↓
Concurrency
     ↓
Persistence
     ↓
Scalability
```

Don't start with:

> "Which design pattern should I use?"

Start with:

> **"What behavior is changing, and which object should own it?"**

The pattern should emerge from the design.

------------------------------------------------------------------------

## Author

**Shyren More**

This repository is a personal collection of LLD practice implementations
focused on preparing for software engineering interviews.

Star/fork the repo if you find the content helpful