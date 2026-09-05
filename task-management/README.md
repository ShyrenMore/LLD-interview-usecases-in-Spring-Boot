# Task Management LLD — Interview Revision Guide

## 1. What This System Is

This is a task-management system designed around three core design patterns:

- **State Pattern** — controls valid task-status transitions.
- **Observer Pattern** — notifies subscribers when a task changes.
- **Strategy Pattern** — allows task sorting algorithms to be swapped at runtime.

The application is also structured as a Spring Boot layered application:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Domain
```

The current repository implementations are intentionally in-memory and use Java collections as a dummy persistence layer.

---

# 2. Architecture

## Domain Layer

Core business objects:

```text
Task
TaskChangeLog
TaskSubscription
Comment
User
DateRange
TaskSearchCriteria
Priority
TaskStatus
UserRole
ChangeType
```

Pattern-related domain classes:

```text
Observer
├── TaskSubject
├── TaskSubscriber
├── EmailSubscriber
└── MobileAppSubscriber

State
├── TaskState
├── TodoState
├── InProgressState
├── ReviewState
├── CompletedState
├── CancelledState
└── InvalidStateTransitionException

Strategy
├── TaskSortingStrategy
├── TaskSortingContext
├── PrioritySortingStrategy
├── DueDateSortingStrategy
└── CreatedDateSortingStrategy
```

## Repository Layer

```text
TaskRepository
UserRepository
TaskChangeLogRepository
TaskSubscriptionRepository
CommentRepository
```

The repositories abstract persistence so the service layer does not need to know whether the underlying storage is a `Map`, JPA, SQL, DynamoDB, etc.

## Service Layer

```text
TaskService
TaskAssignmentService
TaskStateService
TaskNotificationService
```

## Controller Layer

```text
TaskController
TaskAssignmentController
TaskStateController
TaskNotificationController
```

---

# 3. Core Entity: Task

`Task` is the central domain object.

Important fields:

```text
id
title
description
dueDate
priority
status
assigneeId
creatorId
parentTaskId
tags
createdAt
updatedAt
subscribers
currentState
```

A task participates in **both the Observer Pattern and the State Pattern**.

```text
Task
 ├── currentState
 └── subscribers
```

---

# 4. Task Status

Possible statuses:

```text
TODO
IN_PROGRESS
REVIEW
COMPLETED
CANCELLED
```

The key idea is that not every transition is valid.

---

# 5. State Pattern

This is one of the most important interview topics.

The task delegates transition logic to its current state.

The state interface is conceptually:

```java
boolean canTransitionTo(TaskStatus newStatus);

void performTransition(Task task, TaskStatus newStatus);

String getStateName();
```

## State Transition Graph

```text
                  ┌───────────────┐
                  │               ▼
TODO ───────→ IN_PROGRESS ─────→ REVIEW
 │                │                │
 │                │                ├────→ COMPLETED
 │                │                │
 │                │                └────→ IN_PROGRESS
 │                │
 │                └────→ CANCELLED
 │
 └───────────────────────────────────

CANCELLED ─────→ TODO
COMPLETED ─────→ IN_PROGRESS
```

More explicitly:

```text
TODO
 ├──→ IN_PROGRESS
 └──→ CANCELLED

IN_PROGRESS
 ├──→ REVIEW
 └──→ CANCELLED

REVIEW
 ├──→ COMPLETED
 └──→ IN_PROGRESS

COMPLETED
 └──→ IN_PROGRESS

CANCELLED
 └──→ TODO
```

## Why State Pattern?

Without State Pattern, `Task` or `TaskService` could become full of:

```java
if (currentStatus == TODO) {
    ...
} else if (currentStatus == IN_PROGRESS) {
    ...
} else if (...)
```

As the number of states grows, this becomes harder to maintain.

With State Pattern:

```text
TaskState
   ↓
TodoState
InProgressState
ReviewState
CompletedState
CancelledState
```

Each class knows what transitions it allows.

### Interview Answer

> "I used the State Pattern because the allowed behavior depends on the task's current status. Each state encapsulates its valid transitions, which avoids a large conditional block in the task or service and makes the state machine easier to extend."

---

# 6. State Service

`TaskStateService` coordinates state transitions.

Flow:

```text
Controller
   ↓
TaskStateService
   ↓
TaskRepository.findById()
   ↓
Find TaskState corresponding to current status
   ↓
canTransitionTo(newStatus)
   ↓
performTransition()
   ↓
Save Task
   ↓
Notify observers
```

A map is maintained conceptually as:

```text
TODO         → TodoState
IN_PROGRESS  → InProgressState
REVIEW       → ReviewState
COMPLETED    → CompletedState
CANCELLED    → CancelledState
```

Using `EnumMap<TaskStatus, TaskState>` is a natural implementation for enum keys.

---

# 7. Invalid State Transition

Invalid transitions result in:

```java
InvalidStateTransitionException
```

Example:

```text
COMPLETED → CANCELLED
```

is invalid under the current design.

A state can reject the transition instead of allowing the service to know all transition rules.

---

# 8. Observer Pattern

The Observer Pattern is used for task-change notifications.

The subject is:

```text
Task
```

The subscriber interface is:

```java
TaskSubscriber
```

Concrete subscribers include:

```text
EmailSubscriber
MobileAppSubscriber
```

The task maintains:

```java
List<TaskSubscriber> subscribers;
```

Core operations:

```java
attach()
detach()
notifySubscribers()
```

## Observer Flow

```text
Task changes
     ↓
Task.notifySubscribers(...)
     ↓
TaskSubscriber.update(...)
     ├── EmailSubscriber
     └── MobileAppSubscriber
```

### Example

A task's assignee changes:

```text
Old assignee = 1
New assignee = 2
```

The task emits:

```text
ChangeType.ASSIGNED
```

and subscribers receive:

```text
taskId
changeType
oldValue
newValue
```

---

# 9. Change Types

The current change types are:

```text
CREATED
UPDATED
STATUS_CHANGED
ASSIGNED
PRIORITY_CHANGED
```

They provide a common vocabulary for downstream notification logic.

---

# 10. Why Observer Pattern?

Without Observer Pattern:

```text
TaskService
   ↓
EmailService
   ↓
PushService
   ↓
SMSService
   ↓
...
```

The task-management logic becomes tightly coupled to notification mechanisms.

With Observer Pattern:

```text
Task
 ↓
TaskSubscriber
 ├── EmailSubscriber
 ├── MobileAppSubscriber
 └── FutureSubscriber
```

A new notification mechanism can be added without changing the basic task-change mechanism.

### Interview Answer

> "I used the Observer Pattern because a task change can have multiple independent consumers. The task publishes a change, while subscribers decide how to react. This reduces coupling between task-management logic and notification channels."

---

# 11. Observer vs Event-Driven Architecture

This is a likely follow-up.

The current LLD uses **in-process Observer Pattern**.

A production distributed system might use:

```text
Task Service
     ↓
Event / Message Broker
     ↓
Email Consumer
Push Consumer
Audit Consumer
Analytics Consumer
```

Examples of technologies could include Kafka, SNS/SQS, RabbitMQ, etc.

The advantage is that notification processing can become asynchronous and independently scalable.

---

# 12. Subscriber Design

Current subscribers contain placeholder dependencies:

```java
private final String emailService;
private final String pushNotificationService;
```

These are deliberately dummy placeholders.

Production design should inject real interfaces/services, for example:

```text
EmailSubscriber
    ↓
EmailService

MobileAppSubscriber
    ↓
PushNotificationService
```

The actual subscriber should retrieve the users/subscriptions relevant to the task and send notifications.

---

# 13. Strategy Pattern

The system supports multiple task-sorting strategies.

Interface:

```java
TaskSortingStrategy
```

Implementations:

```text
PrioritySortingStrategy
DueDateSortingStrategy
CreatedDateSortingStrategy
```

Context:

```text
TaskSortingContext
```

## Strategy Flow

```text
TaskService
    ↓
TaskSortingContext
    ↓
TaskSortingStrategy
    ├── PrioritySortingStrategy
    ├── DueDateSortingStrategy
    └── CreatedDateSortingStrategy
```

---

# 14. Priority Sorting

Priority values:

```text
LOW
MEDIUM
HIGH
URGENT
```

The priority strategy sorts using the enum's ordering.

Current order is effectively:

```text
URGENT
HIGH
MEDIUM
LOW
```

### Important Caveat

The enum ordinal is being used to represent business priority.

That creates a hidden dependency between:

```text
enum declaration order
```

and:

```text
business priority
```

A more explicit design could give `Priority` a numeric rank:

```java
LOW(1)
MEDIUM(2)
HIGH(3)
URGENT(4)
```

and compare by rank.

This makes the business rule more explicit and safer if the enum declaration order changes.

---

# 15. Due-Date Sorting

`DueDateSortingStrategy` sorts by earliest due date first.

It handles `null` due dates explicitly.

Conceptually:

```text
2026-10-01
2026-10-05
2026-10-10
null
```

depending on the comparator configuration.

---

# 16. Created-Date Sorting

`CreatedDateSortingStrategy` sorts tasks based on creation time.

The strategy uses reverse ordering, so it returns newer tasks first.

---

# 17. Why Strategy Pattern?

Without Strategy Pattern:

```java
if (sortBy.equals("priority")) {
    ...
} else if (sortBy.equals("dueDate")) {
    ...
} else if (sortBy.equals("createdDate")) {
    ...
}
```

The code becomes increasingly difficult to extend.

With Strategy:

```text
TaskSortingStrategy
```

provides a common interface and each strategy implements one algorithm.

### Interview Answer

> "I used the Strategy Pattern because sorting is a variable algorithm. The service can choose the appropriate sorting strategy at runtime without embedding all sorting implementations into the service."

---

# 18. State vs Strategy vs Observer

This comparison is extremely useful in interviews.

| Pattern | Purpose | Example in System |
|---|---|---|
| State | Behavior changes based on object's current state | Task status transitions |
| Strategy | Swap an algorithm/behavior | Task sorting |
| Observer | Notify multiple dependents about a change | Task notifications |

### Easy Memory Trick

```text
State    → "What can I do now?"
Strategy → "How should I do this?"
Observer → "Who should know that this changed?"
```

---

# 19. TaskService

`TaskService` handles core task operations:

```text
createTask()
updateTask()
deleteTask()
searchTasks()
addSubtask()
```

## Create Task

Flow:

```text
TaskController
    ↓
TaskService.createTask()
    ↓
TaskRepository.save()
    ↓
notifySubscribers(CREATED)
```

## Update Task

Flow:

```text
Find existing task
    ↓
Validate
    ↓
Capture old values
    ↓
Update task
    ↓
Save
    ↓
Notify subscribers
    ↓
Update subtask priorities
```

The current code contains TODOs for permissions and stale-task checking.

---

# 20. Optimistic Concurrency / Stale Task

The update flow explicitly identifies a future need for stale-task detection.

The idea is:

```text
User A reads Task version 5
User B reads Task version 5

User A updates → version 6

User B tries to update old version 5
        ↓
Reject update
```

A production implementation could use:

```text
@Version
```

with JPA optimistic locking, or an explicit version field.

### Interview Answer

> "Because multiple users may edit the same task concurrently, I'd use optimistic locking so a stale client cannot overwrite a newer version of the task."

---

# 21. Task Assignment

`TaskAssignmentService` handles assignment.

Flow:

```text
assignTask(taskId, assigneeId)
        ↓
Validate user exists
        ↓
Find task
        ↓
Store old assignee
        ↓
Set new assignee
        ↓
Save
        ↓
Notify ASSIGNED
```

This is a clean example of coordinating multiple domain operations.

---

# 22. Task Notification Service

`TaskNotificationService` manages task subscriptions and task-history access.

Operations:

```text
subscribeToTask()
unsubscribeFromTask()
notifySubscribers()
getTaskHistory()
```

Current unsubscribe and notification methods contain TODOs because the actual subscription persistence and delivery mechanism are not implemented yet.

---

# 23. Task Change Log

`TaskChangeLog` records:

```text
id
taskId
userId
changeType
oldValue
newValue
timestamp
```

This acts as an audit/history concept.

For example:

```text
Task 42
ASSIGNED
oldValue = 1
newValue = 2
timestamp = ...
```

A production system could use these records for:

- Audit trails
- Task history
- Compliance
- Debugging
- User-facing activity feeds

---

# 24. Task Subscription

`TaskSubscription` represents:

```text
userId
taskId
active
```

Conceptually:

```text
User 2
   ↓
subscribed to
   ↓
Task 42
```

The current model supports activation/deactivation rather than physically deleting the relationship.

This is useful for preserving history.

---

# 25. Search Criteria

`TaskSearchCriteria` acts as a criteria/builder-style object.

It can capture:

```text
assigneeId
creatorId
priority
status
dueDateRange
tags
hasSubtasks
sortBy
sortOrder
```

Example:

```java
new TaskSearchCriteria()
    .assigneeId(1)
    .status(TaskStatus.TODO)
    .priority(Priority.HIGH)
    .sortBy("dueDate")
    .sortOrder("asc");
```

This makes it easy to compose optional filters.

---

# 26. Repository Search

Current `TaskRepository.search()` is only a dummy implementation:

```text
return all tasks
```

The intended production behavior would filter by the fields in `TaskSearchCriteria`.

Example filtering flow:

```text
criteria
  ↓
assignee
status
priority
date range
tags
subtask condition
  ↓
filtered tasks
  ↓
sorting strategy
  ↓
result
```

---

# 27. Recursive Subtasks

The `Task` model supports:

```text
parentTaskId
```

This creates a parent-child hierarchy.

Example:

```text
Task: Build Authentication
 ├── Design Database
 ├── Implement Login
 │    ├── Password Validation
 │    └── Session Handling
 └── Write Tests
```

The current methods are placeholders:

```text
getSubtasks()
getAllSubtasks()
hasSubtasks()
getSubtaskCount()
updateSubtaskPriorities()
```

They are intended to be backed by repository/database queries.

---

# 28. Recursive vs Database Responsibility

A common interview discussion:

Should recursive task traversal live in the domain object or repository?

For a production system, avoid pretending the domain object can directly query the database.

A cleaner architecture would be:

```text
TaskService
    ↓
TaskRepository
    ↓
Database
```

The repository could provide:

```text
findImmediateSubtasks(parentTaskId)
findAllDescendants(parentTaskId)
countSubtasks(parentTaskId)
```

Then the service/domain layer can apply business rules.

For a relational database, recursive CTEs may be useful for deep hierarchies.

---

# 29. Spring Boot Conversion

The plain Java implementation was converted into Spring Boot conventions.

## Controllers

Use:

```java
@RestController
@RequestMapping(...)
```

## Services

Use:

```java
@Service
```

## Repositories

Use:

```java
@Repository
```

## Dependency Injection

Use constructor injection, often generated by:

```java
@RequiredArgsConstructor
```

with dependencies declared:

```java
private final TaskRepository taskRepository;
```

---

# 30. Why Constructor Injection?

Interview answer:

> "Constructor injection makes dependencies explicit, lets me keep required dependencies final, improves testability, and avoids hidden dependencies."

Avoid relying on field injection for core dependencies when constructor injection is practical.

---

# 31. Lombok

The project uses Lombok annotations such as:

```text
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
```

## `@Getter`

Generates getter methods.

## `@Setter`

Generates setter methods.

## `@RequiredArgsConstructor`

Generates a constructor for required fields, usually final fields.

## `@Slf4j`

Provides a logger:

```java
log.info(...)
log.warn(...)
log.error(...)
```

This replaces:

```java
System.out.println(...)
```

for application diagnostics.

---

# 32. REST Controller Responsibility

Controllers should be thin.

Expected flow:

```text
HTTP
 ↓
Controller
 ↓
Service
 ↓
Repository
```

Controllers should primarily handle:

```text
request parsing
basic validation
HTTP mapping
response creation
```

Business rules belong in services/domain objects.

---

# 33. Important API Design Consideration

The current controllers were created from the original simulation API, so some endpoints use simple parameters such as:

```text
@RequestParam
@PathVariable
@RequestBody
```

For a production API, a better design would often use request DTOs.

For example:

```java
CreateTaskRequest
UpdateTaskRequest
AssignTaskRequest
```

instead of many individual query parameters.

This gives:

- Cleaner APIs
- Validation with Bean Validation
- Better versioning
- Stronger typing

---

# 34. Exception Handling

The current implementation sometimes uses:

```java
RuntimeException
IllegalArgumentException
```

A production version should introduce meaningful exceptions such as:

```text
TaskNotFoundException
UserNotFoundException
InvalidStateTransitionException
UnauthorizedTaskOperationException
StaleTaskException
```

and centralize API error mapping with:

```java
@RestControllerAdvice
```

---

# 35. Observer Implementation Caveat

The `Task` currently directly constructs:

```java
new EmailSubscriber("emailService")
```

inside its constructor.

This creates coupling between the domain entity and a concrete notification implementation.

From a design perspective, this is not ideal.

A cleaner production architecture would move subscriber registration outside the entity:

```text
TaskService
  ↓
Task
  ↓
Notification/Event mechanism
```

or use application events.

### Interview Answer

> "For the demo, the Task directly registers an EmailSubscriber to illustrate the Observer Pattern. In production I'd avoid constructing infrastructure dependencies inside the domain entity and instead inject or register them at the application/service layer."

---

# 36. Another Important Concurrency Issue

The repository implementations use:

```java
HashMap
```

and manually increment IDs:

```java
nextId++
```

This is fine for a single-threaded simulation but not safe as a production persistence mechanism.

Potential problems:

```text
Concurrent writes
Lost updates
Duplicate IDs
Inconsistent reads
```

Production options depend on architecture:

```text
database transactions
optimistic locking
proper distributed ID generation
ConcurrentHashMap for simple in-memory scenarios
```

---

# 37. Production Persistence

A production system would likely persist:

```text
tasks
users
comments
task subscriptions
task change logs
```

Potential relational structure:

```text
users
tasks
comments
task_subscriptions
task_change_logs
```

Relationships:

```text
User 1 ──── * Task
Task 1 ──── * Comment
Task 1 ──── * TaskSubscription
Task 1 ──── * TaskChangeLog
Task 1 ──── * Child Task
```

---

# 38. Important Production Improvement: Notifications

Current:

```text
Task
 ↓
subscriber.update()
```

This is synchronous and in-process.

A production alternative:

```text
TaskService
    ↓
TaskUpdatedEvent
    ↓
Event Broker
    ↓
Email Consumer
Push Consumer
Audit Consumer
```

Benefits:

- Loose coupling
- Asynchronous processing
- Independent scaling
- Retry support
- Better resilience

---

# 39. Idempotency

This is especially relevant when notifications or external systems are involved.

Suppose a notification request is retried.

You don't want:

```text
same email sent 5 times
```

A production design can use:

```text
eventId
notificationId
deduplication
```

and track processed events.

---

# 40. Validation

Validation should be split logically.

```text
Controller
    ↓
API/request validation

Service
    ↓
Business validation

Domain
    ↓
Domain invariants
```

Examples:

```text
title cannot be empty
due date cannot violate business rules
assignee must exist
task must exist
transition must be legal
user must have permission
parent-child hierarchy must be valid
```

---

# 41. Security Considerations

The current LLD has TODOs around permissions.

A real system must distinguish:

```text
USER
ADMIN
```

and potentially more roles.

Operations may require authorization:

```text
Create task
Assign task
Update task
Delete task
Change status
View history
Manage subscriptions
```

In Spring Boot, authentication/authorization would typically be handled outside the core domain logic using Spring Security and method/endpoint authorization.

---

# 42. Comments

`Comment` stores:

```text
id
taskId
userId
content
createdAt
```

The repository interface is:

```java
Comment save(Comment comment);

List<Comment> findByTaskId(int taskId);
```

The current implementation is a simple in-memory dummy repository.

---

# 43. Repository vs Service

This is a common interview question.

## Repository

Responsible for:

```text
read/write data
query data
persistence abstraction
```

## Service

Responsible for:

```text
business workflows
validation
orchestration
combining multiple repositories
```

Example:

```text
TaskAssignmentService
 ├── UserRepository
 └── TaskRepository
```

The assignment use case belongs in the service because it involves multiple business steps and validation.

---

# 44. 30-Second Interview Explanation

A strong answer to "Explain the design" is:

> "I designed a task-management system using a layered architecture with Domain, Repository, Service, and Controller layers. The main design patterns are State, Observer, and Strategy. State Pattern manages valid task-status transitions such as TODO to IN_PROGRESS to REVIEW and eventually COMPLETED. Observer Pattern allows subscribers like email and mobile notifications to react to task changes without coupling notification logic to the task-management workflow. Strategy Pattern lets us swap sorting algorithms such as priority, due date, and created-date sorting at runtime. Repositories abstract persistence, services handle business orchestration, and controllers expose REST APIs. For production, I would add persistent storage, optimistic locking, centralized exception handling, authorization, asynchronous event-driven notifications, and idempotent processing."

---

# 45. State vs Observer vs Strategy — Memorize This

```text
STATE
"What can the object do right now?"
→ TaskStatus transitions

STRATEGY
"Which algorithm should I use?"
→ Task sorting

OBSERVER
"Who needs to know that something changed?"
→ Email / Push subscribers
```

---

# 46. Likely Interview Questions

## Design Pattern Questions

- Why State Pattern?
- Why Strategy Pattern?
- Why Observer Pattern?
- Why not use `if/else`?
- State vs Strategy?
- Observer vs event-driven architecture?
- Where would Factory Pattern fit?

## Task Questions

- How are task states modeled?
- How do you validate transitions?
- How would you add a new status?
- How would you handle parent/child tasks?
- How would you prevent circular task hierarchies?

## Concurrency Questions

- What happens if two users edit the same task?
- How would you implement optimistic locking?
- Are `HashMap` repositories thread-safe?
- How would you generate unique IDs in a distributed system?

## Spring Questions

- Why `@Service`?
- Why `@Repository`?
- Why constructor injection?
- What does `@RequiredArgsConstructor` do?
- Why use `@RestControllerAdvice`?
- Why use DTOs?

## Notification Questions

- How are subscribers notified?
- What if email delivery fails?
- Would notifications be synchronous?
- How would you make notification delivery retryable?
- How would you prevent duplicate notifications?

---

# 47. Excellent Follow-Up: How Would You Add a New State?

Suppose we add:

```text
BLOCKED
```

Ideally:

```text
BlockedState implements TaskState
```

and define its valid transitions.

Then add:

```text
BLOCKED → BlockedState
```

to the state mapping.

The key point is that the existing states don't need to become giant conditional statements.

That's a major benefit of State Pattern.

---

# 48. Excellent Follow-Up: How Would You Add a New Sorting Algorithm?

Suppose the product team asks for:

```text
Most Recently Updated
```

Create:

```java
RecentlyUpdatedSortingStrategy
```

implement:

```java
TaskSortingStrategy
```

Then select it through the context.

This demonstrates the Open/Closed Principle.

---

# 49. Excellent Follow-Up: How Would You Add SMS Notifications?

Create:

```text
SmsSubscriber implements TaskSubscriber
```

The existing subject/subscriber model does not fundamentally change.

This demonstrates why Observer is useful.

---

# 50. Common Design Improvements to Mention

If asked "What's wrong with this implementation?", mention:

### 1. Domain object creates infrastructure dependency

`Task` directly creates `EmailSubscriber`.

Better to decouple this.

### 2. In-memory persistence

Repositories use `Map`s instead of durable storage.

### 3. Manual IDs

`nextId++` is not distributed-safe.

### 4. Thread safety

`HashMap` is not safe for concurrent access.

### 5. No optimistic locking

Concurrent edits could overwrite each other.

### 6. Search is incomplete

`TaskRepository.search()` currently returns all tasks.

### 7. Subtask methods are placeholders

Recursive queries are not implemented.

### 8. Notification delivery is placeholder logic

No real email/push integration exists.

### 9. Generic exceptions

Use domain-specific exceptions and centralized API handling.

### 10. Hard-coded string sorting values

Prefer enums such as:

```text
TaskSortField
SortDirection
```

instead of:

```text
"priority"
"dueDate"
"createdDate"
"asc"
"desc"
```

---

# 51. Better Modeling for Sort Criteria

Instead of:

```java
private String sortBy;
private String sortOrder;
```

a production model could use:

```text
TaskSortField
    PRIORITY
    DUE_DATE
    CREATED_DATE

SortDirection
    ASC
    DESC
```

This gives compile-time safety and eliminates typo-prone strings.

---

# 52. Better Modeling for Change Events

The current Observer API passes:

```text
int taskId
ChangeType
String oldValue
String newValue
```

As the application grows, a more extensible model would be:

```text
TaskChangedEvent
    taskId
    userId
    changeType
    oldValue
    newValue
    timestamp
    eventId
```

This avoids constantly adding parameters to the Observer interface.

---

# 53. Potential Circular Dependency Problem

Be careful if the following evolves into:

```text
TaskService
   ↓
TaskNotificationService
   ↓
TaskService
```

That could create a circular dependency.

A cleaner architecture might introduce:

```text
TaskService
   ↓
TaskEventPublisher
   ↓
NotificationService
```

This keeps responsibilities separate.

---

# 54. Testability

The current layered design is easy to unit test.

Example:

```text
TaskStateService
    ↓
Mock TaskRepository
```

Test cases:

```text
TODO → IN_PROGRESS      valid
TODO → COMPLETED        invalid
IN_PROGRESS → REVIEW    valid
COMPLETED → CANCELLED   invalid
```

For Observer:

```text
Task
 ↓
Mock TaskSubscriber
```

Verify that:

```text
update(...)
```

is invoked with the expected change.

For Strategy:

```text
Given tasks
 ↓
Priority strategy
 ↓
Expected order
```

---

# 55. Unit Testing Areas to Prioritize

Before an interview, be prepared to write tests for:

```text
State transitions
Observer notification
Task creation
Task assignment
Task search
Sorting
Invalid transitions
Missing task
Missing user
Concurrent/stale updates
```

---

# 56. Final 15 Things to Remember

```text
1. Layered architecture:
   Controller → Service → Repository → Domain

2. State Pattern:
   Encapsulates valid TaskStatus transitions.

3. Observer Pattern:
   Notifies multiple subscribers of task changes.

4. Strategy Pattern:
   Swaps sorting algorithms.

5. Task:
   Central domain object.

6. TaskState:
   Owns state-specific transition rules.

7. TaskSubscriber:
   Defines notification behavior.

8. TaskSortingStrategy:
   Defines sorting behavior.

9. TaskService:
   Core task business operations.

10. TaskAssignmentService:
    Assignment workflow.

11. TaskStateService:
    Status transition workflow.

12. TaskNotificationService:
    Subscription/history workflow.

13. Repository:
    Persistence abstraction.

14. Spring Boot:
    @RestController, @Service, @Repository,
    constructor injection, Lombok.

15. Production:
    DB, optimistic locking, thread safety,
    DTOs, exception handling, authorization,
    asynchronous/idempotent notifications.
```

---

# 57. One-Minute Mental Model

Before entering the interview, remember:

```text
                 TASK
                  │
       ┌──────────┴──────────┐
       │                     │
    STATE                 OBSERVER
       │                     │
 "What can I do?"      "Who should know?"
       │                     │
 TODO                    Email
 IN_PROGRESS             Mobile
 REVIEW
 COMPLETED
 CANCELLED

                  │
                  │
               SERVICE
                  │
               SEARCH
                  │
              STRATEGY
                  │
       ┌──────────┼──────────┐
     Priority   Due Date   Created Date
```

And the overall application:

```text
Client
  ↓
Controller
  ↓
Service
  ├── Domain logic
  ├── State
  ├── Observer
  └── Strategy
  ↓
Repository
  ↓
Persistence
```

---

# 58. Ideal Interview Mindset

Don't merely explain what the code does.

Explain:

```text
Requirement
    ↓
Design decision
    ↓
Pattern / abstraction
    ↓
Trade-off
    ↓
Production improvement
```

Example:

> "Task status changes are state-dependent, so I used State Pattern. This avoids putting transition rules in a large conditional block. The trade-off is that it introduces multiple classes, but the design becomes easier to extend and test."

Another example:

> "Sorting is an independently replaceable algorithm, so I used Strategy Pattern. This makes it easy to add a new sorting rule without modifying the existing strategies."

Another:

> "Task changes can have multiple consumers, so I used Observer Pattern. For production, I'd move from synchronous in-process observers to an event-driven mechanism if notification volume or reliability requirements increase."

That style of explanation demonstrates **LLD understanding**, rather than simply knowing the code.
