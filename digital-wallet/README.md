# Digital Wallet LLD — Interview Revision Notes

> **Purpose:** Quick revision before an LLD / Java / Spring Boot interview.
>
> This README summarizes the Digital Wallet implementation built in this exercise, including the domain model, repository layer, services, gateway/notification abstractions, controllers, concurrency approach, transaction flows, Spring Boot conversion, and key interview discussion points.

---

## 1. Problem Statement

Build a **Digital Wallet System** that supports:

- Creating users
- Creating wallets for users
- Depositing money through a payment gateway
- Handling payment callbacks
- Transferring money between wallets
- Initiating withdrawals
- Viewing account statements
- Suspending / closing / reopening wallets
- Sending notifications
- Supporting multiple payment gateway providers
- Protecting concurrent wallet operations with locking

The implementation is currently **in-memory**. Repositories use `ConcurrentHashMap`.

---

# 2. High-Level Architecture

```text
                         ┌─────────────────────┐
                         │  DigitalWallet      │
                         │  Simulation         │
                         │  CommandLineRunner  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Controllers      │
                         │                     │
                         │ WalletController   │
                         │ TransactionCtrl    │
                         │ AdminController    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │      Services       │
                         │                     │
                         │ WalletService      │
                         │ TransactionService │
                         │ LockService        │
                         └──────┬───────┬──────┘
                                │       │
                 ┌──────────────┘       └────────────────┐
                 ▼                                       ▼
        ┌──────────────────┐                  ┌────────────────────┐
        │   Repositories   │                  │ Infrastructure     │
        │                  │                  │                    │
        │ UserRepository   │                  │ PaymentGateway     │
        │ WalletRepository │                  │ NotificationRouter │
        │ TransactionRepo  │                  │ LockService        │
        └──────────────────┘                  └────────────────────┘
```

---

# 3. Package Structure

```text
com.personal.lld
│
├── DigitalWalletApplication.java
│
├── controller
│   ├── AdminController.java
│   ├── TransactionController.java
│   └── WalletController.java
│
├── domain
│   ├── AccountStatement.java
│   ├── Transaction.java
│   ├── TransactionStatus.java
│   ├── TransactionType.java
│   ├── User.java
│   ├── Wallet.java
│   └── WalletStatus.java
│
├── repository
│   ├── TransactionRepository.java
│   ├── UserRepository.java
│   ├── WalletRepository.java
│   └── impl
│       ├── TransactionRepositoryImpl.java
│       ├── UserRepositoryImpl.java
│       └── WalletRepositoryImpl.java
│
├── service
│   ├── LockService.java
│   ├── TransactionService.java
│   ├── WalletService.java
│   │
│   ├── gateway
│   │   ├── MockPaymentGatewayProvider.java
│   │   ├── PaymentGatewayProvider.java
│   │   └── PaymentGatewayRouter.java
│   │
│   └── notification
│       ├── EmailNotificationChannel.java
│       ├── NotificationChannel.java
│       ├── NotificationMessage.java
│       ├── NotificationRouter.java
│       └── SmsNotificationChannel.java
│
└── simulator
    └── DigitalWalletSimulation.java
```

---

# 4. Domain Model

## User

Represents the customer.

```text
User
├── id
├── username
├── email
├── name
└── createdAt
```

A user can own a wallet.

---

## Wallet

Represents the customer's wallet.

```text
Wallet
├── id
├── accountNumber
├── balanceMinor
├── userId
├── status
├── createdAt
└── updatedAt
```

### WalletStatus

```text
ACTIVE
SUSPENDED
CLOSED
```

Important:

- `ACTIVE` → normal transactions allowed
- `SUSPENDED` → transactions should fail
- `CLOSED` → wallet exists but is not active

---

## Transaction

Represents movement of money.

```text
Transaction
├── id
├── fromWalletId
├── toWalletId
├── amountMinor
├── type
├── status
├── providerRef
├── description
└── timestamp
```

### TransactionType

```text
TRANSFER
DEPOSIT
WITHDRAWAL
```

### TransactionStatus

```text
PENDING
COMPLETED
FAILED
CANCELLED
```

---

## Why `amountMinor`?

Money is represented using the smallest currency unit instead of floating point.

Example:

```text
₹500.00 → 50000 minor units
```

This avoids floating-point precision problems.

### Interview question

**Why not use `double` for money?**

Because floating-point numbers cannot precisely represent many decimal values.

Better choices:

- Minor units using `long`
- `BigDecimal` for decimal monetary calculations

---

# 5. Account Statement

```text
AccountStatement
├── walletId
├── walletAccountNumber
├── transactions
├── startDateUtc
├── endDateUtc
└── currentBalanceMinor
```

The statement retrieves transactions for a wallet within an optional time range.

If dates are absent:

```text
start = 0
end = Long.MAX_VALUE
```

---

# 6. Repository Layer

The repository layer abstracts storage from business logic.

## UserRepository

```java
User save(User user);

Optional<User> findById(String userId);
```

## WalletRepository

```java
Wallet save(Wallet wallet);

Optional<Wallet> findById(String walletId);

Optional<Wallet> findByAccountNumber(String accountNumber);
```

## TransactionRepository

```java
Transaction save(Transaction transaction);

Optional<Transaction> findById(String transactionId);

Optional<Transaction> findByProviderRef(String providerRef);

List<Transaction> findByWalletAndRange(
    String walletId,
    long startUtc,
    long endUtc
);
```

---

# 7. Why Interfaces for Repositories?

Business services depend on abstractions rather than concrete storage implementations.

```text
TransactionService
       │
       ▼
TransactionRepository
       ▲
       │
TransactionRepositoryImpl
```

This makes it easy to replace:

```text
In-memory Map
      ↓
JPA / PostgreSQL
      ↓
DynamoDB
```

without changing the service contract.

---

# 8. In-Memory Repository Implementation

The repositories use:

```java
ConcurrentHashMap
```

Example:

```text
walletsById
    walletId → Wallet

walletIdByAccountNumber
    accountNumber → walletId
```

Why two maps?

Because we need efficient lookup by:

1. Wallet ID
2. Account number

---

# 9. WalletService

Responsibilities:

- Create wallet
- Find wallet
- Check wallet status
- Suspend wallet
- Close wallet
- Reopen wallet

## Create Wallet

Flow:

```text
User ID
   ↓
Generate UUID
   ↓
Generate account number
   ↓
Initial balance = 0
   ↓
Status = ACTIVE
   ↓
Save wallet
```

Account number format:

```text
ACC_<first 8 chars of UUID>
```

---

# 10. TransactionService

This is the most important service in the design.

Responsibilities:

- Transfer
- Deposit initiation
- Deposit callback handling
- Withdrawal initiation
- Account statement generation
- Wallet validation
- Concurrency control

---

# 11. Transfer Flow

```text
transfer(fromAccount, toAccount, amount)
              │
              ▼
      Validate input
              │
              ▼
     Get both active wallets
              │
              ▼
      Generate wallet locks
              │
              ▼
     Sort lock keys
              │
              ▼
       Acquire lock #1
              │
              ▼
       Acquire lock #2
              │
              ▼
   Re-fetch wallets under lock
              │
              ▼
       Check ACTIVE status
              │
              ▼
     Check sufficient balance
              │
              ▼
       Debit source wallet
              │
              ▼
      Credit destination
              │
              ▼
       Save both wallets
              │
              ▼
      Create transaction
              │
              ▼
       Send notification
              │
              ▼
           Return
```

---

# 12. Transfer Validation

The implementation checks:

### Null accounts

```java
if (fromAccountNumber == null || toAccountNumber == null)
```

### Same account

```java
if (fromAccountNumber.equals(toAccountNumber))
```

### Positive amount

```java
if (amountMinor <= 0)
```

### Active wallets

Both source and destination must be:

```text
WalletStatus.ACTIVE
```

### Sufficient balance

```java
fromWallet.getBalanceMinor() >= amountMinor
```

---

# 13. Why Lock Both Wallets?

A transfer modifies **two shared resources**:

```text
Wallet A balance -= amount
Wallet B balance += amount
```

If two threads operate concurrently, we can get inconsistent results.

Example:

```text
Initial A = 100
```

Two concurrent transfers:

```text
Thread 1: transfer 80
Thread 2: transfer 80
```

Without locking:

```text
Thread 1 reads 100
Thread 2 reads 100

Thread 1 → writes 20
Thread 2 → writes 20

Both transactions may succeed
Actual balance should not allow both
```

Locking serializes the critical section.

---

# 14. Why Sort Lock Keys?

This is a very important interview point.

Suppose:

```text
Thread 1:
Lock A → waits for B

Thread 2:
Lock B → waits for A
```

This creates a deadlock.

Instead, always acquire locks in deterministic order:

```text
sort(A, B)

Both threads:
Lock A → Lock B
```

Therefore one thread waits for the other instead of creating a circular wait.

### Key interview phrase

> "I acquire multiple locks in a globally consistent order to prevent circular wait and therefore avoid deadlocks."

---

# 15. Current Lock Implementation

```java
Map<String, ReentrantLock>
```

with:

```java
ConcurrentHashMap
```

and:

```java
tryLock(timeout)
```

Current timeout:

```text
5000 ms
```

If acquisition fails:

```text
IllegalStateException
```

---

# 16. Limitation of ReentrantLock

`ReentrantLock` works only inside **one JVM instance**.

It does NOT provide distributed locking.

If we deploy:

```text
Application Instance A
Application Instance B
```

each instance has its own lock map.

Therefore:

```text
Instance A lock != Instance B lock
```

### Production solution

Use a distributed lock, for example:

```text
Redis
```

using a pattern such as:

```text
SET key value NX EX
```

or another properly designed distributed locking mechanism.

---

# 17. Deposit Flow

Deposit is asynchronous because an external payment gateway is involved.

```text
Client
  │
  ▼
initiateDeposit()
  │
  ▼
Validate amount
  │
  ▼
Validate wallet
  │
  ▼
Select payment gateway
  │
  ▼
Initiate payment with provider
  │
  ▼
Receive providerRef
  │
  ▼
Create PENDING transaction
  │
  ▼
Return transaction
```

The wallet balance is **not immediately credited**.

---

# 18. Deposit Callback Flow

Later, payment provider sends:

```text
providerRef
status
```

Flow:

```text
Callback
   │
   ▼
Find transaction by providerRef
   │
   ▼
Already COMPLETED/FAILED?
   │
   ├── Yes → return
   │
   └── No
        │
        ▼
   Lock destination wallet
        │
        ▼
   Load wallet
        │
        ▼
   COMPLETED?
      /     \
    Yes      No
     │        │
     ▼        ▼
Credit     FAILED
wallet     status
     │
     ▼
Save wallet
     │
     ▼
Update transaction
     │
     ▼
Send notification
```

---

# 19. Idempotency

The callback handler checks:

```java
if (tx.getStatus() == COMPLETED
        || tx.getStatus() == FAILED) {
    return;
}
```

This protects against duplicate callbacks.

Example:

```text
Payment gateway
      │
      ├── callback → COMPLETED
      │
      └── callback → COMPLETED again
```

Without idempotency:

```text
Balance +500
Balance +500
```

Result:

```text
Balance +1000 ❌
```

With the status check:

```text
First callback  → credit
Second callback → ignored
```

### Interview phrase

> "External callbacks are at-least-once in nature, so the handler must be idempotent."

---

# 20. Payment Gateway Design

The abstraction is:

```java
PaymentGatewayProvider
```

Methods:

```java
String getName();

String initiatePayment(
    String accountNumber,
    long amountMinor,
    String paymentMethod,
    Map<String, String> paymentDetails
);

boolean verifyCallback(
    String providerRef,
    String status
);
```

---

# 21. Why PaymentGatewayProvider Interface?

We don't want:

```java
if (gateway.equals("stripe")) {
    ...
} else if (gateway.equals("razorpay")) {
    ...
}
```

Instead:

```text
PaymentGatewayProvider
       │
       ├── MockPaymentGatewayProvider
       ├── StripePaymentGatewayProvider
       ├── RazorpayPaymentGatewayProvider
       └── ...
```

This follows the **Open/Closed Principle**.

Adding a new gateway should not require rewriting `TransactionService`.

---

# 22. PaymentGatewayRouter

Responsibilities:

- Register providers
- Select provider
- Resolve provider

Example:

```text
"mock"
   ↓
PaymentGatewayRouter
   ↓
MockPaymentGatewayProvider
```

If a preferred gateway is supplied:

```text
Use preferred gateway
```

Otherwise:

```text
Use any registered provider
```

---

# 23. Strategy Pattern

`PaymentGatewayProvider` can be explained as a **Strategy Pattern**.

The algorithm varies:

```text
Payment Gateway
       │
       ├── Mock
       ├── Stripe
       ├── Razorpay
       └── Adyen
```

The caller depends on the common interface.

---

# 24. Notification Design

Abstraction:

```java
NotificationChannel
```

Implementations:

```text
EmailNotificationChannel
SmsNotificationChannel
```

Potential future implementations:

```text
WhatsAppNotificationChannel
PushNotificationChannel
```

---

# 25. NotificationRouter

The router maps:

```text
"email" → EmailNotificationChannel
"sms"   → SmsNotificationChannel
```

Then:

```java
notificationRouter.send(
    "email",
    message
);
```

The service doesn't need to know how email is actually sent.

---

# 26. Another Strategy Pattern

Notification channels are also naturally modeled using the **Strategy Pattern**:

```text
NotificationChannel
        │
        ├── Email
        ├── SMS
        ├── Push
        └── WhatsApp
```

---

# 27. Withdrawal Flow

Current implementation:

```text
Validate amount
      ↓
Validate active wallet
      ↓
Check sufficient balance
      ↓
Create PENDING transaction
      ↓
Save transaction
```

Important:

### The current implementation does NOT debit the wallet.

Why?

The comment indicates that an external payout service would complete the withdrawal.

Therefore:

```text
Withdrawal requested
        ↓
PENDING
        ↓
External payout processing
        ↓
SUCCESS / FAILURE
```

The actual production implementation should have a withdrawal callback/state transition.

---

# 28. Account Statement Flow

```text
accountNumber
      ↓
Find wallet
      ↓
Determine date range
      ↓
Find transactions involving wallet
      ↓
Filter by timestamp
      ↓
Sort by timestamp
      ↓
Return AccountStatement
```

Transaction belongs to wallet if:

```java
walletId.equals(t.getFromWalletId())
    ||
walletId.equals(t.getToWalletId())
```

---

# 29. Spring Boot Conversion

The original Java implementation manually created objects:

```java
new WalletService(...)
new TransactionService(...)
new WalletController(...)
```

Spring Boot replaces this with dependency injection.

Example:

```java
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
}
```

Spring creates and injects dependencies.

---

# 30. Important Spring Annotations

## `@SpringBootApplication`

Application entry point.

```java
@SpringBootApplication
public class DigitalWalletApplication {
}
```

---

## `@Service`

Marks business logic classes.

```java
@Service
public class TransactionService {
}
```

---

## `@Repository`

Marks persistence components.

```java
@Repository
public class WalletRepositoryImpl {
}
```

---

## `@Component`

Generic Spring-managed component.

Used for:

```text
Payment providers
Notification channels
Simulation
```

---

## `@RestController`

Marks REST API controllers.

```java
@RestController
@RequestMapping("/api/wallets")
```

---

# 31. Lombok

The converted implementation uses Lombok to reduce boilerplate.

Common annotations:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
```

### `@Data`

Generates:

- getters
- setters
- equals
- hashCode
- toString

### `@RequiredArgsConstructor`

Creates constructor for `final` fields.

This supports constructor injection.

### `@Slf4j`

Creates an SLF4J logger.

---

# 32. Why Constructor Injection?

Prefer:

```java
private final WalletRepository walletRepository;

public WalletService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
}
```

over field injection:

```java
@Autowired
private WalletRepository walletRepository;
```

Benefits:

- Dependencies are explicit
- Easier unit testing
- Supports immutable fields
- Prevents partially initialized objects

---

# 33. REST API Surface

## Wallet

### Create wallet

```http
POST /api/wallets?userId=<userId>
```

### Get balance

```http
GET /api/wallets/{accountNumber}/balance
```

### Get statement

```http
GET /api/wallets/{accountNumber}/statement
```

Optional:

```text
startUtc
endUtc
```

---

## Transactions

### Transfer

```http
POST /api/transactions/transfer
```

Parameters:

```text
fromAccountNumber
toAccountNumber
amountMinor
description
```

### Deposit

```http
POST /api/transactions/deposit
```

### Deposit callback

```http
POST /api/transactions/deposit/callback
```

### Withdrawal

```http
POST /api/transactions/withdraw
```

---

## Admin

### Suspend

```http
PUT /api/admin/wallets/{accountNumber}/suspend
```

### Close

```http
PUT /api/admin/wallets/{accountNumber}/close
```

### Reopen

```http
PUT /api/admin/wallets/{accountNumber}/reopen
```

---

# 34. Simulation

`DigitalWalletSimulation` is a Spring `CommandLineRunner`.

It executes automatically when the Spring Boot application starts.

Flow:

```text
1. Create Alice
2. Create Bob
3. Create Wallet A
4. Create Wallet B
5. Deposit 50,000 into A
6. Simulate payment callback
7. Transfer 20,000 A → B
8. Withdraw 10,000 from B
9. Get statement for A
10. Suspend B
11. Attempt A → B transfer
12. Verify expected failure
```

The simulator is useful for demonstrating the complete flow without requiring an external API client.

---

# 35. Structured Logging

Instead of:

```java
System.out.println(...)
```

use:

```java
@Slf4j
```

and:

```java
log.info(
    "Transfer completed. transactionId={}, status={}",
    transactionId,
    status
);
```

Benefits:

- Log levels
- Structured parameters
- Better production observability
- Integration with CloudWatch / ELK / Splunk etc.
- Avoids unnecessary string construction

Typical levels:

```text
TRACE
DEBUG
INFO
WARN
ERROR
```

---

# 36. Important Interview Questions

## Q1. How do you prevent two transfers from corrupting wallet balances?

Use locking around the critical section.

For a transfer, lock both wallets before reading/updating balances.

---

## Q2. Why lock in sorted order?

To prevent deadlocks caused by circular waiting.

---

## Q3. Is `ReentrantLock` suitable for multiple application instances?

No.

It is JVM-local.

For distributed deployments, use a distributed coordination mechanism such as Redis or a database-based locking strategy.

---

## Q4. Why use `ConcurrentHashMap`?

Because multiple threads may access the in-memory repositories concurrently.

It provides thread-safe concurrent map operations.

However, thread-safe individual map operations do **not automatically make multi-step business transactions atomic**.

---

## Q5. Is the transfer atomic?

Not truly in the database-transaction sense.

The current implementation uses locks to protect the in-memory operation:

```text
debit A
credit B
save A
save B
save transaction
```

If persistence were a real database, we'd need transaction management such as:

```java
@Transactional
```

plus appropriate database locking/optimistic concurrency control.

---

## Q6. What happens if saving A succeeds but saving B fails?

The current in-memory implementation does not provide rollback.

In production, this is a major concern.

Possible solution:

```text
Database transaction
+
@Transactional
```

so either all changes commit or all roll back.

---

# 37. Concurrency vs Transaction

Very important distinction.

### Locking solves:

```text
Concurrent access
```

Example:

```text
Two threads modifying same wallet
```

### Database transaction solves:

```text
Atomic persistence
```

Example:

```text
Debit A
Credit B
Create transaction record
```

You generally need to think about **both**.

---

# 38. Potential Production Improvements

The current implementation is intentionally simple for LLD.

A production design would likely add:

### Persistence

```text
PostgreSQL / MySQL / DynamoDB
```

instead of `ConcurrentHashMap`.

### Transaction management

```text
@Transactional
```

where appropriate.

### Distributed locking

```text
Redis
```

or database locking.

### Idempotency

Store a unique idempotency key for payment requests/callbacks.

### Authentication

```text
OAuth2 / JWT
```

### Authorization

Ensure users can only access their own wallets.

### Validation

Use:

```text
Bean Validation
@Valid
@NotNull
@Positive
```

etc.

### Exception handling

Centralize API errors with:

```java
@RestControllerAdvice
```

### Observability

Add:

```text
Metrics
Tracing
Structured logs
Correlation IDs
```

### Payment callback security

The current:

```java
verifyCallback(...)
```

is a mock and always returns `true`.

Production should verify:

```text
Signature
Timestamp
Provider reference
Request authenticity
```

---

# 39. Important Design Patterns

## Strategy Pattern

Used for:

```text
PaymentGatewayProvider
NotificationChannel
```

---

## Repository Pattern

Used to abstract persistence:

```text
WalletRepository
TransactionRepository
UserRepository
```

---

## Router / Registry

Used by:

```text
PaymentGatewayRouter
NotificationRouter
```

to select an implementation dynamically.

---

## Dependency Injection

Spring manages object creation and dependencies.

---

# 40. SOLID Principles Demonstrated

## S — Single Responsibility

Examples:

```text
WalletService
    → wallet operations

TransactionService
    → transaction operations

PaymentGatewayRouter
    → gateway selection

NotificationRouter
    → notification routing
```

---

## O — Open/Closed

Add:

```text
NewPaymentGatewayProvider
```

without modifying core transaction logic.

---

## L — Liskov Substitution

Any implementation of:

```java
PaymentGatewayProvider
```

can be used by the router.

---

## I — Interface Segregation

Small focused interfaces:

```text
WalletRepository
TransactionRepository
UserRepository
NotificationChannel
PaymentGatewayProvider
```

---

## D — Dependency Inversion

Services depend on:

```text
Repository interfaces
Provider interfaces
Channel interfaces
```

rather than concrete implementations.

---

# 41. Transaction State Machines

A useful way to explain the design in an interview:

## Deposit

```text
          ┌─────────────┐
          │   PENDING   │
          └──────┬──────┘
                 │
          ┌──────┴───────┐
          ▼              ▼
     COMPLETED         FAILED
```

## Withdrawal

Current implementation:

```text
PENDING
```

A production implementation should support:

```text
PENDING
   │
   ├── COMPLETED
   │
   └── FAILED
```

## Transfer

Current implementation completes synchronously:

```text
TRANSFER → COMPLETED
```

A more elaborate system might introduce:

```text
INITIATED
   ↓
PROCESSING
   ↓
COMPLETED / FAILED
```

---

# 42. Important Edge Cases

Be ready to discuss:

- Null account numbers
- Same source and destination account
- Zero amount
- Negative amount
- Insufficient balance
- Suspended wallet
- Closed wallet
- Missing wallet
- Missing transaction
- Duplicate payment callback
- Unknown payment provider
- Lock timeout
- Interrupted thread
- Concurrent transfers
- Duplicate provider reference
- Notification failure
- Payment gateway failure
- Database failure during transfer
- Application crash between operations

---

# 43. Important Bugs / Limitations to Recognize

Knowing the limitations of your own design is a strong interview signal.

## 1. Withdrawal does not debit balance

The current code only creates a `PENDING` transaction.

A production withdrawal workflow needs an external payout completion flow.

---

## 2. Notification failures can affect the transfer call

Currently notification happens after saving the transaction.

If notification throws an exception, the transfer may have already succeeded but the API call could appear failed.

Better design:

```text
Persist transaction
      ↓
Commit
      ↓
Publish event
      ↓
Async notification
```

For example:

```text
Kafka / RabbitMQ / SQS
```

---

## 3. Hard-coded notification recipient

The current implementation uses:

```text
user@example.com
```

Production should resolve the user's actual contact details.

---

## 4. Callback verification is mocked

The provider's callback should be cryptographically verified.

---

## 5. No persistence

Restarting the application loses all data.

---

## 6. No authentication/authorization

Any caller could potentially access any account.

---

## 7. No database-level atomicity

Locks protect concurrent JVM operations but do not provide durable transactional semantics.

---

# 44. How to Explain the Entire Design in 60 Seconds

A good interview answer:

> "I designed the wallet system using a layered architecture. The domain contains User, Wallet and Transaction models. Repository interfaces abstract persistence, with in-memory ConcurrentHashMap implementations for this exercise. WalletService handles wallet lifecycle operations, while TransactionService handles deposits, transfers, withdrawals and statements.
>
> For transfers, I lock both wallets and acquire the locks in sorted order to prevent deadlocks. I re-fetch the wallets after acquiring the locks so that I operate on the latest state. Deposits are asynchronous: the transaction starts as PENDING and the wallet is credited only after a verified payment callback. The callback is idempotent so duplicate callbacks don't credit the wallet twice.
>
> Payment gateways and notification channels are modeled using interfaces and routers, which allows implementations to be added without changing the core transaction logic. In Spring Boot, dependencies are managed using constructor injection, and controllers expose the wallet operations as REST APIs.
>
> For production, I would replace the in-memory repositories with a transactional database, use distributed locking or database concurrency control for multi-instance deployments, add authentication, idempotency keys, secure callback verification, centralized exception handling and asynchronous event-driven notifications."

---

# 45. If the Interviewer Asks "What Would You Improve?"

Answer in this order:

```text
1. Database persistence
2. Atomic transactions
3. Distributed concurrency control
4. Idempotency
5. Payment callback verification
6. Authentication / authorization
7. Async notifications
8. Centralized exception handling
9. Observability
10. API validation
```

This shows that you understand the difference between an **LLD exercise** and a **production system**.

---

# 46. Quick Revision Cheat Sheet

```text
DOMAIN
------
User
Wallet
Transaction
AccountStatement

WALLET
------
ACTIVE
SUSPENDED
CLOSED

TRANSACTION
-----------
TRANSFER
DEPOSIT
WITHDRAWAL

STATUS
------
PENDING
COMPLETED
FAILED
CANCELLED

PATTERNS
--------
Repository
Strategy
Router / Registry
Dependency Injection

CONCURRENCY
-----------
ConcurrentHashMap
ReentrantLock
Lock both wallets
Sort locks → prevent deadlock

DEPOSIT
-------
Create PENDING transaction
↓
Payment gateway
↓
providerRef
↓
Callback
↓
Lock wallet
↓
Credit
↓
COMPLETED

IDEMPOTENCY
-----------
Ignore already COMPLETED / FAILED callback

MONEY
-----
Use minor units / long
Avoid double

SPRING
------
@SpringBootApplication
@Service
@Repository
@Component
@RestController
@RequiredArgsConstructor
@Slf4j

PRODUCTION
----------
DB
@Transactional
Distributed lock
Idempotency key
Secure callback verification
AuthN/AuthZ
Async events
Observability
Exception handling
```

---

# 47. Final Mental Model

When revising immediately before the interview, remember these **five core ideas**:

### 1. Wallet = State

```text
balance + status
```

### 2. Transaction = Money Movement Record

```text
from + to + amount + type + status
```

### 3. Transfer = Two-wallet Concurrency Problem

```text
lock both
→ deterministic order
→ validate
→ debit + credit
```

### 4. Deposit = Asynchronous + Idempotent

```text
PENDING
→ callback
→ credit once
→ COMPLETED
```

### 5. Gateway/Notification = Pluggable Strategies

```text
Interface
   ↓
Multiple implementations
   ↓
Router selects implementation
```

If you can clearly explain these five concepts, you can explain most of the design confidently in an LLD interview.
