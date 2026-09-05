# Vending Machine LLD — Interview Revision Guide

## 1. Overall Architecture

The system follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Domain
```

### Domain Layer

Contains the core business entities and the State Pattern:

- `VendingMachine`
- `Product`
- `Inventory`
- `CashBox`
- `PaymentRequest`
- `Transaction`
- `Denomination`
- `ProductCategory`
- `TransactionStatus`
- `Recovery`
- `RecoveryStatus`
- `VendingMachineState`
- `IdleState`
- `ProcessingPaymentState`
- `DispensingState`
- `InvalidStateTransitionException`

### Repository Layer

Provides data-access abstraction. The current implementation uses in-memory `Map`s rather than a database:

- `VendingMachineRepository`
- `ProductRepository`
- `PaymentRepository`
- `RecoveryRepository`

### Service Layer

Contains business operations:

- `VendingMachineService`
- `PaymentService`
- `AdminService`
- `RecoveryService`

### Controller Layer

Exposes REST APIs:

- `VendingMachineController`
- `PaymentController`
- `AdminController`
- `RecoveryController`

The Spring Boot version uses annotations such as:

```java
@RestController
@Service
@Repository
@RequiredArgsConstructor
@Slf4j
```

---

# 2. Most Important Design Pattern: State Pattern

This is the most important design concept in the implementation.

Instead of putting all vending-machine behavior inside one giant `if/else` or `switch`, the machine delegates behavior to its current state.

```java
private VendingMachineState currentState;
```

The state interface defines operations such as:

```java
processPayment(...)
cancelPayment(...)
getStateName()
```

The main states are:

```text
IDLE
   ↓
PROCESSING_PAYMENT
   ↓
DISPENSING
   ↓
IDLE
```

## Why State Pattern?

Without the State Pattern:

```java
if (state == IDLE) {
    ...
} else if (state == PROCESSING_PAYMENT) {
    ...
} else if (state == DISPENSING) {
    ...
}
```

This becomes difficult to maintain as the number of states increases.

With the State Pattern:

```java
machine.getCurrentState().processPayment(...)
```

Each state owns its own behavior.

### Interview Answer

> "I used the State Pattern because the vending machine behaves differently depending on its current lifecycle state. Instead of putting state-specific conditions inside `VendingMachine`, I encapsulated those behaviors into separate state classes. This makes adding a new state easier and keeps `VendingMachine` from becoming a large conditional class."

---

# 3. VendingMachine

`VendingMachine` is the central aggregate.

It maintains:

```text
id
location
currentState
currentTransaction
inventory
cashBox
operational
```

Important methods include:

```java
processPayment()
cancelPayment()
setState()
addProduct()
dispenseProduct()
addCash()
removeCash()
```

The machine does not implement every state-specific operation itself.

Instead:

```java
return currentState.processPayment(this, request);
```

This is the key State Pattern interaction.

---

# 4. Payment Flow

Suppose the user wants Cola for `$2.50` and inserts `$5`.

The conceptual flow is:

```text
Client
  ↓
PaymentController
  ↓
PaymentService
  ↓
VendingMachine.processPayment()
  ↓
IdleState.processPayment()
  ↓
Create Transaction
  ↓
Add payment
  ↓
PROCESSING_PAYMENT
  ↓
DISPENSING
  ↓
Dispense Product
  ↓
IDLE
```

`PaymentService` is responsible for coordinating the operation.

The `Transaction` stores:

```text
transaction ID
machine ID
product ID
amount inserted
amount required
change returned
status
timestamp
```

---

# 5. Transaction Lifecycle

Possible statuses:

```text
PENDING
COMPLETED
FAILED
CANCELLED
```

Important methods:

```java
addPayment()
isPaymentComplete()
getRemainingAmount()
cancel()
fail()
```

When sufficient money is inserted:

```java
status = COMPLETED;
```

## Important Interview Point

A transaction status and a vending-machine state are different concepts.

For example:

```text
Machine state     = PROCESSING_PAYMENT
Transaction status = PENDING
```

They represent different dimensions of the system.

---

# 6. CashBox

`CashBox` maintains the available denominations:

```java
Map<Denomination, Integer>
```

Example:

```text
$1   → 10
$5   → 5
$10  → 3
$20  → 2
...
```

It tracks:

```text
totalAmount
```

Important operations:

```java
addDenomination()
removeDenomination()
hasSufficientChange()
calculateChange()
```

## Change Algorithm

The current implementation uses a **greedy approach**:

```text
$100
$50
$20
$10
$5
$1
```

It takes as many high-value denominations as possible.

### Interview Question: Is Greedy Always Optimal?

No.

A greedy algorithm is not guaranteed to produce the minimum number of coins/notes for an arbitrary denomination system.

### Interview Answer

> "The current implementation uses a greedy algorithm, which is reasonable for common currency denominations. However, for arbitrary denominations, greedy is not guaranteed to be optimal. A bounded coin-change or dynamic-programming approach could be required."

---

# 7. Inventory

Inventory tracks:

```text
productId
vendingMachineId
quantity
minThreshold
```

Useful methods:

```java
isLowStock()
isOutOfStock()
addQuantity()
removeQuantity()
```

For example:

```java
quantity <= minThreshold
```

means low stock.

```java
quantity <= 0
```

means out of stock.

---

# 8. Repository Layer

Repositories abstract data access.

For example, `PaymentRepository` maintains:

```java
Map<Integer, Transaction> transactions
```

and provides methods such as:

```java
saveTransaction()
findById()
findByMachine()
getTransactionHistory()
```

## Why a Repository?

Interview answer:

> "The repository separates persistence concerns from business logic. Today the implementation can be an in-memory map; tomorrow it could be backed by JPA, DynamoDB, or another database without forcing the service layer to change significantly."

---

# 9. Why `@Repository`?

In Spring Boot:

```java
@Repository
public class PaymentRepository
```

allows Spring to manage the class as a bean.

Services can then receive it through dependency injection.

---

# 10. Service Layer

Services contain business operations and orchestration.

## PaymentService

Responsible for:

```text
process payment
cancel payment
get payment status
cash-box operations
transaction history
```

## AdminService

Responsible for:

```text
restocking
cash collection
sales reports
inventory status
system health
```

## RecoveryService

Responsible for:

```text
detect incomplete operations
create recovery entries
process pending recoveries
recover machine state
startup recovery
```

## VendingMachineService

Responsible for:

```text
available products
product details
inventory status
product availability
stock
```

---

# 11. Recovery System

The system assumes the machine could fail while processing a transaction.

For example:

```text
PROCESSING_PAYMENT
        ↓
Power failure
```

After restart, the machine may not know whether the operation completed.

Therefore `Recovery` stores:

```text
machineId
transactionId
state
status
createdAt
completedAt
```

Possible recovery statuses:

```text
PENDING
COMPLETED
```

## Recovery Flow

```text
Startup
   ↓
checkAndRecover()
   ↓
Find machines not in IDLE
   ↓
Find incomplete transactions
   ↓
Create Recovery
   ↓
Process Recovery
   ↓
Reset machine to IDLE
```

### Interview Explanation

> "Recovery is essentially a lightweight persistence mechanism for interrupted operations. If a machine is found in a non-safe state, we identify the incomplete transaction and replay or compensate for the operation depending on the state."

---

# 12. Compensation

For `PROCESSING_PAYMENT`, the implementation performs:

```java
transaction.cancel();
```

Conceptually this represents a compensating action / refund.

For `DISPENSING`, the current implementation marks the transaction completed.

### Important Caveat

The current implementation contains TODOs for actual refund and dispensing recovery logic.

Do **not** describe these as production-grade payment/refund implementations in an interview.

A better phrasing is:

> "The current LLD models the recovery flow and compensation points, while the actual external refund and dispensing reconciliation would be implemented as separate production integrations."

---

# 13. Spring Boot Conversion

## Dependency Injection

Instead of manually doing:

```java
new PaymentService(...)
```

Spring manages the dependency graph.

We use:

```java
@RequiredArgsConstructor
```

with:

```java
private final PaymentRepository paymentRepository;
```

Spring injects dependencies through the generated constructor.

## Why Constructor Injection?

Interview answer:

> "Constructor injection makes dependencies explicit, supports immutability with final fields, makes classes easier to test, and avoids hidden dependencies."

---

# 14. Lombok

The project uses annotations such as:

```java
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
```

## `@Getter` / `@Setter`

Removes repetitive boilerplate.

## `@RequiredArgsConstructor`

Generates a constructor for required fields, typically `final` fields.

## `@Slf4j`

Provides structured logging:

```java
log.info(...)
log.warn(...)
log.error(...)
```

instead of:

```java
System.out.println(...)
```

---

# 15. REST Controllers

Controllers expose the application through HTTP.

For example:

```java
@RestController
@RequestMapping("/api/payments")
```

Payment endpoints include concepts such as:

```text
POST /api/payments/machines/{machineId}
POST /api/payments/machines/{machineId}/transactions/{transactionId}/cancel
GET  /api/payments/machines/{machineId}/transactions/{transactionId}/status
GET  /api/payments/machines/{machineId}/transactions
GET  /api/payments/machines/{machineId}/cash
```

The controller should ideally remain thin:

```text
HTTP request
   ↓
Controller
   ↓
Service
```

It should not contain business logic.

---

# 16. Important SOLID Principles

## Single Responsibility Principle

Each class has a focused role:

```text
VendingMachine → machine behavior
CashBox        → cash handling
Transaction    → transaction state
Repository     → persistence
Service        → business orchestration
Controller     → HTTP/API layer
```

## Open/Closed Principle

The State Pattern makes it easier to add states such as:

```text
MaintenanceState
OutOfServiceState
```

without heavily modifying `VendingMachine`.

## Dependency Inversion

Higher-level business logic is separated from persistence and infrastructure concerns through repositories and services.

---

# 17. Questions Interviewers May Ask

## Why State Pattern instead of Strategy?

Good distinction:

> "Strategy generally represents a replaceable algorithm chosen by a client or context, whereas State represents an object's behavior changing as its internal state changes. Here, the vending machine's behavior changes as the machine transitions through its lifecycle states, so State Pattern is the more natural fit."

---

## Why Not Put Everything Inside VendingMachine?

Because that creates a **God class**.

You would end up with:

```text
payment logic
cash logic
inventory logic
recovery logic
state transitions
transaction handling
```

all inside one class.

That would violate separation of concerns and make the class difficult to test and extend.

---

## Why Repository If We're Just Using Maps?

Because the repository is an abstraction around data access.

The implementation can evolve:

```text
Map
 ↓
JPA Repository
 ↓
Database
```

without significantly changing the business layer.

---

## Where Should Validation Happen?

A useful split is:

```text
Controller → request/API validation
Service    → business validation
Domain     → invariants/domain rules
```

Business checks can include:

```text
machine exists?
product exists?
enough stock?
enough change?
valid state?
valid quantity?
```

---

# 18. Production Improvements

This is one of the best sections to know for a follow-up question like:

> "What would you change if this were a production system?"

## 1. Money Representation

The current code uses:

```java
double
```

For monetary values, use:

```java
BigDecimal
```

or integer cents.

Reason:

`double` can introduce floating-point precision problems.

---

## 2. Transaction IDs

The repository currently generates IDs using:

```java
nextTransactionId++
```

This is not appropriate for distributed or concurrent production workloads.

Potential alternatives:

```text
database sequence
UUID
distributed ID generator
```

---

## 3. Thread Safety

The repositories use:

```java
HashMap
```

which is not thread-safe.

Concurrent requests can therefore cause race conditions.

Potential solutions depend on the architecture:

```text
ConcurrentHashMap
database transactions
synchronization / locking
distributed locking
```

---

## 4. Persistence

The current repositories are in-memory.

A production system would persist at least:

```text
machines
inventory
transactions
payments
cash
recovery records
```

---

## 5. Recovery Must Be Idempotent

This is a very important distributed-systems concept.

Running recovery twice should **not** result in:

```text
double refund
double dispensing
double transaction completion
```

Before taking a recovery action, the system should determine whether it has already been performed.

### Interview Answer

> "Recovery operations should be idempotent. If the recovery process is retried because of a crash or timeout, executing it again must not produce an incorrect second side effect."

---

## 6. API Error Handling

Instead of returning:

```java
null
```

or throwing generic:

```java
RuntimeException
```

introduce domain-specific exceptions and centralized handling with:

```java
@RestControllerAdvice
```

and map failures to meaningful HTTP status codes.

---

# 19. Important Architectural Issue: `Thread.sleep()`

The simulation includes:

```java
Thread.sleep(1000);
```

to simulate payment processing.

That is acceptable for a toy/demo application, but should not be used to block a Spring Boot request thread in production.

### Interview Answer

> "The sleep is only simulating an external payment-processing delay. In production, I would not block a request thread with `Thread.sleep`; I would use an asynchronous workflow, an external payment provider callback, or an event-driven processing model."

---

# 20. Potential Issue: Product as a `Map` Key

The current machine stores inventory as:

```java
Map<Product, Integer>
```

This can be problematic because `Product` does not currently define logical `equals()` and `hashCode()` semantics.

Two different Java `Product` objects representing the same real-world product could therefore be treated as different keys.

A cleaner design would be:

```java
Map<Integer, Integer>
```

where:

```text
productId → quantity
```

or implement proper `equals()` and `hashCode()` based on product identity.

### Interview Answer

> "For inventory, I'd generally key by `productId` rather than a mutable `Product` object. That gives the inventory map a stable identity and avoids equality/hash-code issues."

---

# 21. Object Relationships

Think of the system like this:

```text
VendingMachine
 ├── currentState ─────→ VendingMachineState
 ├── currentTransaction → Transaction
 ├── inventory ─────────→ Product → ProductCategory
 └── cashBox ───────────→ Denomination


PaymentService
 ├── VendingMachineRepository
 └── PaymentRepository

AdminService
 ├── VendingMachineRepository
 ├── ProductRepository
 └── PaymentRepository

RecoveryService
 ├── VendingMachineRepository
 ├── RecoveryRepository
 └── PaymentRepository
```

---

# 22. 30-Second Interview Explanation

A strong answer to "Explain your design" is:

> "I designed the vending machine using a layered architecture with Domain, Repository, Service, and Controller layers. The core design pattern is the State Pattern because the machine transitions between states such as Idle, Processing Payment, and Dispensing, and each state encapsulates its own behavior. Transactions track the payment lifecycle, CashBox handles denomination-level cash management and change calculation, and repositories abstract persistence. I also added a Recovery mechanism to handle interrupted transactions and reset machines to a safe state. In the Spring Boot implementation, controllers expose REST APIs, services contain business orchestration, dependencies are injected through constructors using Lombok, and repositories are currently in-memory but can later be replaced with database-backed implementations."

---

# 23. Quick Revision: The 10 Things to Remember

```text
1. State Pattern
   → Machine behavior changes with state.

2. VendingMachine
   → Central aggregate coordinating current machine state,
     inventory, transaction, and cash box.

3. Transaction
   → Tracks the payment lifecycle.

4. CashBox
   → Manages denominations and change.

5. Inventory
   → Manages stock levels.

6. Repository
   → Persistence/data-access abstraction.

7. Service
   → Business orchestration.

8. Controller
   → REST/API layer.

9. Recovery
   → Handles interrupted operations and restores a safe state.

10. Production Improvements
    → BigDecimal, persistence, thread safety,
      idempotency, proper exception handling,
      asynchronous payment processing.
```

---

# 24. Last-Minute Interview Checklist

Before the interview, make sure you can explain these without looking at the code:

### Design
- Why State Pattern?
- Why not Strategy?
- What are the machine states?
- How does a state transition happen?
- What happens when payment is cancelled?

### Payment
- How is a transaction created?
- How do you determine whether payment is complete?
- How is change calculated?
- Why is `double` a problem for money?
- How would you integrate a real payment provider?

### Inventory
- How is stock maintained?
- What happens when the product is out of stock?
- Why might `Map<Product, Integer>` be a problem?

### Recovery
- Why do we need recovery?
- What happens after power failure?
- How do you prevent double refunds?
- How would you make recovery idempotent?

### Spring Boot
- Why `@Service`?
- Why `@Repository`?
- Why constructor injection?
- What does `@RequiredArgsConstructor` do?
- Why `@Slf4j` instead of `System.out.println()`?
- Why should controllers be thin?

### Scalability
- What happens with multiple concurrent requests?
- Are `HashMap`s safe?
- How would you persist transactions?
- How would you generate unique IDs?
- How would you handle multiple vending machines?

---

# 25. Ideal Interview Mindset

Don't just describe the code.

Explain the **reason behind the design**:

```text
Requirement
    ↓
Design decision
    ↓
Design pattern / abstraction
    ↓
Trade-off
    ↓
Production improvement
```

For example:

> "The machine has state-dependent behavior, so I used the State Pattern. The benefit is encapsulation of state-specific behavior and easier extension. The trade-off is that we introduce more classes. For a production system, I'd additionally make the workflow asynchronous and ensure recovery operations are idempotent."

That style of explanation demonstrates LLD understanding rather than just familiarity with the implementation.
