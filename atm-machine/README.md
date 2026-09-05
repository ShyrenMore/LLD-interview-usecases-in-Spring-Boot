# ATM Machine — LLD Interview Revision Guide

This README is a **last-minute interview revision sheet** for the ATM Machine LLD.

The design uses:

- **State Pattern** for ATM lifecycle/state transitions
- **Strategy Pattern** for transaction behavior
- **Repository Pattern** for persistence/data access
- **Service Layer** for business orchestration
- **REST Controllers** for API boundaries
- **Spring Boot + Lombok + SLF4J** conventions in the converted implementation

The implementation is intentionally LLD-focused rather than a production-complete banking system.

---

# 1. The 60-Second Interview Explanation

> "I model an ATM as a stateful system. The ATM can be idle, have a card inserted, authenticate the customer, select a transaction, and complete that transaction.  
>
> I use the **State Pattern** because the valid operations depend heavily on the ATM's current state. For example, you should not withdraw money while the ATM is idle.  
>
> I use the **Strategy Pattern** because different transaction types—withdrawal, deposit, and balance inquiry—have different algorithms and can evolve independently.  
>
> I keep persistence behind repository abstractions so the domain and services are not tightly coupled to the storage mechanism. In the Spring Boot version, repositories are `@Repository`, business classes are `@Service`, and API entry points are `@RestController`.  
>
> The most important consistency requirement is that a withdrawal must not debit the account without successfully reserving/dispensing the cash, and vice versa."

That is the core story.

---

# 2. High-Level Architecture

```text
                   ┌─────────────────────┐
                   │    REST Controller  │
                   │ Admin/Card/Session/ │
                   │ Transaction/ATM     │
                   └──────────┬──────────┘
                              │
                              ▼
                   ┌─────────────────────┐
                   │       Service       │
                   │ ATM / Card / Session│
                   │ Transaction / Admin  │
                   └──────────┬──────────┘
                              │
             ┌────────────────┴────────────────┐
             ▼                                 ▼
    ┌──────────────────┐             ┌──────────────────┐
    │     Repository   │             │  Domain/State/   │
    │  Persistence     │             │ Strategy Logic   │
    └────────┬─────────┘             └────────┬─────────┘
             │                                │
             ▼                                ▼
    In-memory stores                  ATM + States +
                                      Transactions +
                                      Strategies
```

## Main layers

### Controller

API boundary.

Examples:

- `ATMController`
- `AdminController`
- `CardController`
- `SessionController`
- `TransactionController`

Responsibilities:

- Accept request data
- Call the appropriate service/state operation
- Return the result
- Handle API-level error responses in a production implementation

---

### Service

Business/application orchestration.

Examples:

- `ATMService`
- `CardService`
- `SessionService`
- `TransactionService`
- `AdminService`

Responsibilities:

- Coordinate repositories and domain objects
- Perform business-level operations
- Manage application workflow
- Hide persistence implementation from controllers

---

### Repository

Persistence abstraction.

Examples:

- `ATMRepository`
- `AccountRepository`
- `CardRepository`
- `CashDrawerRepository`
- `SessionRepository`
- `TransactionRepository`
- `AdminUserRepository`

The supplied implementation uses `ConcurrentHashMap`, so it behaves as an in-memory repository.

---

### Domain

Core business objects:

- `ATM`
- `Account`
- `Card`
- `CashDrawer`
- `Denomination`
- `Session`
- `Transaction`
- `TransactionType`
- `TransactionStatus`

---

# 3. Most Important Design Patterns

## 3.1 State Pattern

### Why?

The behavior of the ATM depends on its current state.

Without State Pattern, we might end up with:

```java
if (state == IDLE) {
    ...
} else if (state == CARD_INSERTED) {
    ...
} else if (state == AUTHENTICATED) {
    ...
}
```

spread across a large `ATM` class.

That becomes difficult to maintain.

With State Pattern:

```text
ATM
 |
 +-- currentState
       |
       +-- IdleState
       +-- CardInsertedState
       +-- AuthenticatedState
       +-- TransactionSelectedState
       +-- TransactionCompletedState
       +-- OutOfServiceState
```

The state object decides what operation is valid.

---

## 3.2 ATM State Machine

The intended flow is:

```text
                 ┌──────────────┐
                 │     IDLE     │
                 └──────┬───────┘
                        │ insert card
                        ▼
             ┌───────────────────────┐
             │   CARD_INSERTED       │
             └───────────┬───────────┘
                         │ enter PIN
                         ▼
             ┌───────────────────────┐
             │    AUTHENTICATED      │
             └───────────┬───────────┘
                         │ select transaction
                         ▼
             ┌───────────────────────┐
             │ TRANSACTION_SELECTED  │
             └───────────┬───────────┘
                         │ process
                         ▼
             ┌───────────────────────┐
             │ TRANSACTION_COMPLETED │
             └───────────┬───────────┘
                         │ end/eject
                         ▼
                 ┌──────────────┐
                 │     IDLE     │
                 └──────────────┘

                    ATM OFFLINE
                         │
                         ▼
                ┌────────────────┐
                │ OUT_OF_SERVICE │
                └────────────────┘
```

### Interview answer

**Question:** Why State Pattern?

**Answer:**

> "Because the same operation has different validity depending on the ATM state. State Pattern localizes those state-specific rules and avoids a large conditional-driven ATM class."

---

# 4. State Classes

## `ATMState`

Defines the state operations.

Typical operations include:

```java
insertCard(...)
enterPin(...)
selectTransaction(...)
processTransaction(...)
ejectCard(...)
endSession(...)
```

The exact implementation can reject unsupported operations by throwing:

```java
InvalidATMOperationException
```

---

## `AbstractATMState`

Provides default behavior for unsupported operations.

This avoids repeating the same exception handling in every state.

Conceptually:

```java
public abstract class AbstractATMState implements ATMState {

    @Override
    public void ejectCard(ATM atm) {
        throw new InvalidATMOperationException(...);
    }

    // same idea for unsupported operations
}
```

---

## `IdleState`

Typical responsibilities:

- Accept card
- Start the ATM session through the attached services
- Move the ATM to the next state

---

## `CardInsertedState`

Typical responsibilities:

- Accept PIN
- Authenticate the card
- Move to authenticated state if successful

---

## `AuthenticatedState`

Typical responsibilities:

- Allow transaction selection
- Move to `TransactionSelectedState`

---

## `TransactionSelectedState`

Typical responsibilities:

- Accept the requested transaction
- Delegate the operation to `TransactionService`
- Store/update the last transaction
- Move to `TransactionCompletedState`

---

## `TransactionCompletedState`

Typical responsibilities:

- Finish the transaction
- Allow next appropriate action
- Eventually return ATM to `IdleState`

---

## `OutOfServiceState`

Represents an ATM that is unavailable.

Typical behavior:

- Reject normal customer operations
- Allow administrative recovery through `ATMService`

---

# 5. Strategy Pattern

The ATM supports multiple transaction types:

```java
WITHDRAW
DEPOSIT
BALANCE
```

Instead of putting all algorithms inside `TransactionService`, the design uses:

```text
TransactionStrategy
       |
       +-- WithdrawalStrategy
       +-- DepositStrategy
       +-- BalanceInquiryStrategy
```

The service selects the strategy using:

```java
Map<TransactionType, TransactionStrategy>
```

---

# 6. Why Strategy Pattern?

Each transaction has different logic.

### Withdrawal

Needs to consider:

- Account balance
- Withdrawal limits
- ATM cash availability
- Denomination availability
- Cash inventory update
- Account debit

### Deposit

Needs to consider:

- Notes supplied
- Total deposit amount
- Cash inventory increase
- Account credit

### Balance

Needs:

- Account lookup
- Balance retrieval

Putting these algorithms in separate strategies gives:

- easier testing
- cleaner `TransactionService`
- easier addition of new transaction types

Example future strategy:

```text
MINI_STATEMENT
TRANSFER
PIN_CHANGE
CASH_DEPOSIT
CARDLESS_WITHDRAWAL
```

---

# 7. Strategy vs State — VERY IMPORTANT

A common interview question.

### State

Answers:

> **"What can the ATM do right now?"**

Example:

```text
IDLE
CARD_INSERTED
AUTHENTICATED
```

### Strategy

Answers:

> **"How should this particular transaction be performed?"**

Example:

```text
WITHDRAW
DEPOSIT
BALANCE
```

### Easy memory trick

```text
State    = CURRENT CONDITION
Strategy = CHOSEN ALGORITHM
```

---

# 8. Core Domain Classes

## ATM

Represents the physical ATM.

Contains concepts such as:

- `id`
- location
- online/offline status
- current state
- current session
- last transaction
- attached services/state coordination

The ATM delegates state-dependent behavior to `currentState`.

---

## Account

Represents a bank account.

Important fields include:

- account ID
- customer name
- balance
- active status
- daily withdrawal limit
- daily withdrawal used

Important concepts:

```text
available balance
daily withdrawal limit
daily withdrawal consumed
```

---

## Card

Represents a customer card.

Important responsibilities:

- Card identity
- Linked account
- Expiry
- PIN retry tracking
- Block/unblock behavior

A card should become blocked after the configured failed PIN attempts.

---

## Session

Represents one ATM interaction.

Contains:

- session ID
- ATM ID
- card ID
- account ID
- active flag

Typical lifecycle:

```text
startSession()
      ↓
perform operations
      ↓
endSession()
```

---

## Transaction

Represents one financial operation.

Important fields/concepts:

- transaction ID
- ATM ID
- session ID
- transaction type
- amount
- creation time
- status
- timeout/expiry information

Statuses:

```text
PENDING
SUCCESS
FAILED
```

---

## CashDrawer

Represents ATM cash inventory.

Conceptually:

```text
Denomination -> Number of notes
```

Example:

```text
500 -> 10
200 -> 25
100 -> 50
```

The `CashDrawer` exposes operations such as:

```java
addNotes(...)
removeNotes(...)
getTotalCash()
```

---

## Denomination

Enum representing supported note values.

The exact values depend on the implementation.

---

# 9. Repository Layer

All repositories use:

```java
ConcurrentHashMap
```

This is useful for the LLD because it gives thread-safe access to the map structure.

Typical repository operations:

```java
save(...)
findById(...)
update(...)
```

Specialized examples:

```java
findActiveByATM(...)
findBySession(...)
findByATMAndTimeRange(...)
updateCashInventory(...)
```

### Why repository abstraction?

Without repositories, services directly manipulate storage:

```java
Map<String, Account> accountStore
```

That tightly couples business logic to persistence.

With repositories:

```text
Service
   ↓
Repository
   ↓
In-memory / DB / Redis / external persistence
```

The storage implementation can change without changing the service API.

---

# 10. Why `ConcurrentHashMap`?

For the in-memory LLD:

```java
private final Map<String, Account> accountStore =
        new ConcurrentHashMap<>();
```

It provides concurrent access to the map itself.

### Important interview nuance

`ConcurrentHashMap` does **not** automatically make the entire financial operation atomic.

This is a crucial point.

For example:

```text
check balance
    ↓
debit account
    ↓
remove ATM notes
```

can still race between threads.

For real ATM behavior, we need stronger synchronization / locking / transactional guarantees.

---

# 11. Service Layer

## ATMService

Responsibilities:

- Take ATM offline
- Bring ATM online
- Retrieve ATM
- Audit cash

Offline:

```text
online = false
state = OutOfServiceState
```

Online:

```text
online = true
state = IdleState
```

---

## CardService

Responsibilities:

- Validate card
- Authenticate card
- Handle PIN retries
- Eject card

Future production work:

```text
bank/card network validation
PIN hashing
HSM integration
caching
fraud/risk checks
```

---

## SessionService

Responsibilities:

- Start session
- Get current session
- End session
- Handle session timeout

Current implementation has a placeholder account mapping:

```java
String accountId = "ACC_001";
```

In production this would come from the banking/card system.

---

## TransactionService

Responsibilities:

- Expose transaction operations
- Choose the correct strategy
- Persist the transaction
- Validate/acknowledge transactions

Strategy lookup:

```text
TransactionType
       ↓
Map<TransactionType, TransactionStrategy>
       ↓
Concrete Strategy
```

---

## AdminService

Responsibilities:

- Admin login
- Cash refill
- Cash audit

Production enhancements:

- Proper authentication
- Authorization
- Audit trail
- Role-based permissions
- Dual-control/refill approval

---

# 12. Controllers

## `CardController`

Typical operations:

```text
POST /api/atms/{atmId}/card/insert
POST /api/atms/{atmId}/card/eject
POST /api/atms/{atmId}/card/authenticate
```

---

## `SessionController`

Typical operations:

```text
POST /api/sessions
POST /api/sessions/{sessionId}/end
```

---

## `TransactionController`

Typical operations:

```text
GET  /api/transactions/balance/{sessionId}
POST /api/transactions/withdraw/{sessionId}
POST /api/transactions/deposit/{sessionId}
```

---

## `ATMController`

Typical operations:

```text
POST /api/atms/{atmId}/offline
POST /api/atms/{atmId}/online
GET  /api/atms/{atmId}/cash
GET  /api/atms/{atmId}
```

---

## `AdminController`

Typical operations:

```text
POST /api/admin/login
POST /api/admin/atms/{atmId}/cash/refill
GET  /api/admin/atms/{atmId}/cash/audit
```

These APIs are representative LLD endpoints, not a production-ready API contract.

---

# 13. Transaction Flow — Withdrawal

This is one of the most important flows to explain.

```text
Client
  │
  ▼
TransactionController
  │
  ▼
ATM
  │
  ▼
Current State
  │
  ▼
TransactionSelectedState
  │
  ▼
TransactionService
  │
  ▼
WithdrawalStrategy
  │
  ├── Validate account
  ├── Validate balance
  ├── Validate daily limit
  ├── Validate ATM cash
  ├── Select notes
  ├── Update cash inventory
  └── Debit account
  │
  ▼
TransactionRepository
```

---

# 14. Withdrawal Invariants

These are excellent interview talking points.

A successful withdrawal should guarantee:

```text
1. amount > 0

2. account exists

3. account is active

4. sufficient balance

5. withdrawal <= per-transaction limit
   if such a limit exists

6. daily withdrawal usage <= daily limit

7. ATM has enough cash

8. ATM has a valid note combination

9. cash inventory is updated consistently

10. account balance is updated consistently

11. transaction becomes SUCCESS
```

If any check fails:

```text
NO CASH DISPENSED
NO ACCOUNT DEBIT
TRANSACTION = FAILED
```

---

# 15. The Hardest Part: Cash + Account Consistency

Suppose the ATM does:

```text
1. Debit account
2. Try to dispense cash
```

and cash dispensing fails.

Now:

```text
Account = debited
ATM cash = unchanged
```

This is incorrect.

The reverse ordering can create the opposite problem:

```text
1. Dispense cash
2. Account debit fails
```

Now the customer got cash without paying.

---

# 16. How to Discuss This in an Interview

Say:

> "The withdrawal is a distributed consistency problem even if my LLD is in-memory. Account balance and ATM cash inventory must be updated atomically from the business perspective. In production I'd use transactional locking or a coordination mechanism, with idempotency and compensation where required."

Depending on architecture, possible techniques include:

```text
DB transaction
row-level locking
distributed lock
reservation
two-phase/compensating workflow
idempotency key
ledger-based accounting
```

---

# 17. Cash Dispensing Algorithm

The sample withdrawal strategy uses a greedy-style approach.

Example:

```text
Requested amount = 900

Available:
500
200
100
100
...
```

Possible result:

```text
500 + 200 + 100 + 100
```

The algorithm generally takes larger denominations first.

### Important caveat

Greedy is not universally optimal for arbitrary denomination sets.

For example, with unusual denominations, greedy may fail even when a valid combination exists.

For a production ATM:

- denominations are controlled
- supported note combinations are known
- exact dispensing rules should be validated

A stronger LLD answer can mention:

```text
greedy
+
backtracking/DP where necessary
+
inventory constraints
```

---

# 18. Deposit Flow

```text
Client
  ↓
TransactionController
  ↓
ATM
  ↓
TransactionSelectedState
  ↓
TransactionService
  ↓
DepositStrategy
  ├── calculate note value
  ├── validate notes
  ├── add notes to drawer
  └── credit account
```

The amount is calculated as:

```text
Σ denomination.value × numberOfNotes
```

---

# 19. Balance Inquiry Flow

```text
TransactionController
      ↓
ATM
      ↓
TransactionSelectedState
      ↓
TransactionService
      ↓
BalanceInquiryStrategy
      ↓
Account information
      ↓
TransactionRepository
```

No cash inventory modification is needed.

---

# 20. PIN Authentication

Current LLD behavior:

```text
card not found
     → fail

card blocked
     → fail

PIN valid
     → reset retry count
     → success

PIN invalid
     → decrement retry count
```

The supplied code contains a placeholder for actual bank-side PIN validation.

### Production design

Do not store plaintext PINs.

Use:

```text
PIN
 ↓
secure input
 ↓
HSM / bank authentication service
```

Not:

```text
PIN
 ↓
database plaintext
```

---

# 21. Money Representation

The implementation uses integer minor units such as:

```java
long balanceMinorUnits;
long amountMinorUnits;
```

This is preferable to floating point for monetary values.

Example:

```text
₹100.50
```

could be represented as:

```text
10050 paise
```

The exact currency representation depends on the system.

### Interview phrase

> "I avoid floating point for money because of precision issues. I prefer integer minor units or a decimal type such as BigDecimal."

---

# 22. Thread Safety

This is a major production concern.

Imagine:

```text
Account balance = 10,000

ATM request A checks balance
ATM request B checks balance

A withdraws 8,000
B withdraws 8,000
```

Without proper synchronization, both could observe the same old balance.

### Better design

Use an atomic/transactional operation:

```text
check + debit
```

as one business operation.

Potential implementation options:

```text
SELECT ... FOR UPDATE
optimistic locking/version column
atomic database update
distributed lock
ledger reservation
```

---

# 23. Idempotency

Imagine the client sends:

```text
withdraw ₹10,000
```

The request succeeds, but the response is lost.

The client retries.

Without idempotency:

```text
₹10,000 withdrawn
₹10,000 withdrawn again
```

### Solution

Attach an idempotency key:

```text
requestId = ABC123
```

Store the result:

```text
ABC123 → SUCCESS, transactionId=TXN001
```

A retry returns the same result instead of performing the withdrawal again.

---

# 24. Transaction Lifecycle

The model has:

```text
PENDING
SUCCESS
FAILED
```

Conceptually:

```text
           ┌──────────┐
           │ PENDING  │
           └────┬─────┘
                │
          ┌─────┴─────┐
          ▼           ▼
      SUCCESS       FAILED
```

A stronger production model might also have:

```text
INITIATED
AUTHORIZED
PROCESSING
COMPLETED
FAILED
REVERSED
EXPIRED
```

---

# 25. Session Management

A session links:

```text
ATM
 +
Card
 +
Account
```

Typical lifecycle:

```text
Card inserted
      ↓
Session created
      ↓
Authentication
      ↓
Transactions
      ↓
Session timeout / end
      ↓
Session closed
      ↓
Card ejected
```

### Production concerns

- absolute timeout
- inactivity timeout
- card removal
- ATM power failure
- network failure
- automatic logout
- cleanup of sensitive in-memory data

---

# 26. ATM Availability

The ATM has an operational state:

```text
online
offline
```

Administrative flow:

```text
takeOffline()
     ↓
online = false
state = OutOfServiceState
```

Recovery:

```text
bringOnline()
     ↓
online = true
state = IdleState
```

In a real system, additional health states could exist:

```text
OUT_OF_CASH
CARD_READER_FAILURE
PRINTER_FAILURE
NETWORK_FAILURE
MAINTENANCE
```

This can lead to a broader machine-health model separate from the customer transaction state machine.

---

# 27. Important Distinction: ATM State vs ATM Health

This is a good advanced interview observation.

Current LLD mixes operational behavior and machine availability.

A production design could separate:

```text
Customer Session State
    IDLE
    CARD_INSERTED
    AUTHENTICATED
    TRANSACTION_SELECTED
    COMPLETED
```

from:

```text
ATM Health
    ONLINE
    OUT_OF_SERVICE
    LOW_CASH
    HARDWARE_FAILURE
```

This avoids one massive state machine.

---

# 28. Exception Handling

Current LLD uses:

```java
InvalidATMOperationException
```

for invalid state operations.

Examples:

```text
withdraw while IDLE
eject card when no card exists
select transaction before authentication
```

### Production Spring Boot design

Use:

```text
@ControllerAdvice
```

to map domain exceptions into HTTP responses.

For example:

```text
InvalidATMOperationException
        ↓
HTTP 409 / 400
```

with structured JSON:

```json
{
  "code": "INVALID_ATM_OPERATION",
  "message": "Transaction cannot be selected in current state"
}
```

---

# 29. DTOs

The current controllers expose domain objects directly.

In a production REST API, prefer:

```text
Request DTO
Response DTO
```

instead of exposing domain entities.

Example:

```java
WithdrawRequest {
    String sessionId;
    long amountMinorUnits;
    String idempotencyKey;
}
```

This gives:

- API/domain separation
- validation
- versioning
- security
- controlled response shape

---

# 30. Dependency Injection

Spring version uses constructor injection.

Preferred style:

```java
@Service
@RequiredArgsConstructor
public class ATMService {

    private final ATMRepository atmRepository;
    private final CashDrawerRepository cashDrawerRepository;
}
```

### Why constructor injection?

- Dependencies are explicit
- Easier unit testing
- Supports immutable `final` fields
- Prevents partially initialized objects
- Better than field injection for most application code

---

# 31. Lombok Usage

Common annotations used:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
```

### Important distinction

`@RequiredArgsConstructor` works well when dependencies are:

```java
private final SomeService someService;
```

Lombok generates the constructor automatically.

---

# 32. Logging

Instead of:

```java
System.out.println(...)
```

the Spring version uses:

```java
@Slf4j
```

and:

```java
log.info(...)
log.error(...)
```

Why?

Because production logging needs:

- levels
- structured log pipelines
- centralized collection
- correlation IDs
- configurable output
- observability

---

# 33. Main Simulation

The original standalone simulation was converted to a Spring Boot style:

```java
@SpringBootApplication
public class ATMSimulation implements CommandLineRunner
```

Spring starts the application and then runs the simulation.

The important dependency graph is:

```text
Spring
  ↓
Repositories
  ↓
Services
  ↓
Controllers
  ↓
ATM/domain state machine
```

For a real application, controllers would normally be triggered by HTTP requests instead of a simulation runner.

---

# 34. Current LLD Weaknesses — Know These Before Interview

The implementation is an educational LLD, so there are intentional simplifications.

## 34.1 Hard-coded account ID

Current session creation has a placeholder:

```java
String accountId = "ACC_001";
```

Production:

```text
Card
 ↓
Bank/Card Network
 ↓
Associated Account
```

---

## 34.2 Placeholder PIN validation

The code contains a placeholder for bank-side PIN verification.

Production:

```text
ATM
 ↓
secure authentication
 ↓
issuer/bank/HSM
```

---

## 34.3 Direct service attachment to ATM

The current stateful ATM model uses an `attachServices(...)` style integration.

A cleaner production design could inject an application/facade dependency that coordinates the state machine while keeping the domain layer less coupled to Spring services.

---

## 34.4 Transaction atomicity is incomplete

The code demonstrates the concepts but does not implement a real atomic financial transaction across:

```text
account
cash drawer
transaction record
```

This is one of the biggest production gaps.

---

## 34.5 API error handling is basic

Some controllers return:

```java
null
false
```

A production API should use:

```text
HTTP status codes
typed error responses
@ControllerAdvice
```

---

## 34.6 Domain objects are exposed directly

Use DTOs in production.

---

## 34.7 Security is simplified

Real ATM systems need:

- encryption
- secure PIN handling
- HSM
- card authentication
- tamper detection
- secure communication
- audit logs
- fraud detection

---

# 35. Production Architecture

A stronger real-world architecture could be:

```text
                    ATM Terminal
                         │
                         ▼
                 ATM Application API
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
        Session Service        Transaction Service
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                ▼
                 Account         Cash/Cassette     Ledger
                  Service          Service          Service
                    │                │                │
                    └────────────┬───┴────────────────┘
                                 ▼
                              Event Bus
                                 │
                   ┌─────────────┼─────────────┐
                   ▼             ▼             ▼
                 Audit          Fraud       Monitoring
```

External dependencies could include:

```text
Core Banking
Card Network
HSM
Fraud Service
Ledger
Notification Service
```

---

# 36. SOLID Principles in This LLD

## Single Responsibility

Examples:

```text
ATM
CardService
SessionService
TransactionService
CashDrawer
```

have distinct responsibilities.

---

## Open/Closed Principle

Strategy Pattern makes this easier.

Adding:

```text
MiniStatementStrategy
```

does not require rewriting withdrawal logic.

State Pattern similarly isolates state behavior.

---

## Liskov Substitution

All concrete transaction strategies should be usable where:

```java
TransactionStrategy
```

is expected.

Same for concrete ATM states implementing:

```java
ATMState
```

---

## Interface Segregation

The `SupportsNotes` interface is a useful example.

Only states that need denomination/note operations need the note-specific capability.

This avoids forcing every ATM state to implement irrelevant methods.

---

## Dependency Inversion

Services depend on repository abstractions/concepts rather than directly embedding the storage implementation.

In the Spring version, Spring injects these dependencies.

---

# 37. Why `SupportsNotes` Exists

The ATM state model contains a capability interface:

```java
SupportsNotes
```

It lets a state expose note-related processing only where needed.

This is a small but useful design idea:

```text
ATMState
    = general ATM behavior

SupportsNotes
    = note-specific behavior
```

It avoids polluting every state with cash-specific operations.

---

# 38. Questions Interviewers Are Likely to Ask

## Q1. Why State Pattern?

Because ATM behavior depends on current state and state transitions.

---

## Q2. Why Strategy Pattern?

Because transaction algorithms vary and should be independently replaceable/testable.

---

## Q3. Why not just use an enum and switch?

Possible for a small LLD.

But as states grow, a switch can become difficult to maintain.

State Pattern scales behavior by class.

---

## Q4. What happens if the ATM is offline?

`ATMService.takeOffline()`:

```text
online = false
currentState = OutOfServiceState
```

The normal customer operations are then rejected.

---

## Q5. How do you prevent two users from withdrawing the same balance?

Use transactional/atomic balance update, locking, optimistic concurrency, or a ledger/reservation mechanism.

`ConcurrentHashMap` alone is insufficient.

---

## Q6. How do you prevent duplicate withdrawal on client retry?

Use an idempotency key tied to the transaction request.

---

## Q7. What if ATM cash is insufficient?

Transaction fails before debit/cash dispensing.

---

## Q8. What if the ATM has enough total cash but not the required denominations?

The withdrawal algorithm must find a valid note combination.

Otherwise fail the transaction.

---

## Q9. What if cash dispensing succeeds but account debit fails?

This is a consistency problem.

Need strong transactional coordination / reservation / compensation and reconciliation.

---

## Q10. What if the ATM loses power after dispensing?

Persist the transaction state and reconcile with cash hardware + account ledger.

This is one reason real ATM systems rely on durable transaction/ledger records rather than only in-memory objects.

---

## Q11. Why store money in minor units?

Avoid floating point precision errors.

---

## Q12. Why use repository classes?

To isolate persistence from business logic.

---

## Q13. Why constructor injection?

Explicit, immutable, testable dependencies.

---

## Q14. What happens after 3 wrong PIN attempts?

The card should be blocked according to the configured policy.

The sample design tracks retry count and blocking behavior.

---

## Q15. How would you add a new transaction type?

Create:

```text
NewTransactionStrategy
```

and register it against the new:

```java
TransactionType
```

Then the service can discover it through the strategy map.

---

# 39. Design Improvements You Can Proactively Mention

When the interviewer asks:

> "How would you improve this?"

Mention these in roughly this order:

### 1. Atomicity

```text
cash + account + transaction
```

must remain consistent.

### 2. Idempotency

Prevent duplicate withdrawals caused by retries.

### 3. Persistence

Replace in-memory maps with durable database storage.

### 4. Concurrency control

Use optimistic/pessimistic locking or atomic updates.

### 5. Security

Use secure PIN verification and HSM/bank integration.

### 6. DTO + validation

Do not expose domain objects through REST.

### 7. Global exception handling

Use `@ControllerAdvice`.

### 8. Observability

Add:

```text
metrics
distributed tracing
structured logs
audit events
```

### 9. Reconciliation

Reconcile:

```text
ATM cash
bank ledger
transaction records
```

especially after failures.

### 10. Hardware abstraction

Introduce interfaces for:

```text
CardReader
CashDispenser
CashAcceptor
ReceiptPrinter
Keypad
Display
```

This would make the design much closer to a real ATM.

---

# 40. Hardware Abstraction — Strong Interview Extension

A better ATM design could contain:

```text
ATM
 ├── CardReader
 ├── CashDispenser
 ├── CashAcceptor
 ├── Keypad
 ├── Display
 └── ReceiptPrinter
```

Interfaces:

```java
interface CardReader {
    Card readCard();
    void ejectCard();
}

interface CashDispenser {
    void dispense(Map<Denomination, Integer> notes);
}

interface CashAcceptor {
    Map<Denomination, Integer> accept();
}
```

Benefits:

- easier unit testing
- hardware/vendor independence
- easier simulator implementation
- clean separation between physical device APIs and business logic

---

# 41. Testing Strategy

## Unit tests

Test each state independently:

```text
IdleState
CardInsertedState
AuthenticatedState
TransactionSelectedState
```

Test each strategy:

```text
WithdrawalStrategy
DepositStrategy
BalanceInquiryStrategy
```

Test services:

```text
CardService
SessionService
TransactionService
ATMService
```

---

## Important withdrawal test cases

```text
withdraw positive amount
withdraw exactly available balance
withdraw greater than balance
withdraw greater than daily limit
withdraw with insufficient ATM cash
withdraw with unavailable denomination combination
withdraw from inactive account
duplicate withdrawal request
```

---

## State tests

```text
insert card while idle → success

insert card while transaction completed → invalid

enter PIN before card insertion → invalid

select transaction before authentication → invalid

withdraw before selecting transaction → invalid
```

---

# 42. Example Test Matrix

| Scenario | Expected |
|---|---|
| ATM offline + insert card | Reject |
| Insert valid card | Move to `CARD_INSERTED` |
| Invalid card | Reject |
| Wrong PIN | Retry count decreases |
| Too many PIN failures | Card blocked |
| Valid PIN | Move to `AUTHENTICATED` |
| Select withdrawal | Move to `TRANSACTION_SELECTED` |
| Valid withdrawal | Transaction succeeds |
| Insufficient balance | Transaction fails |
| Insufficient ATM cash | Transaction fails |
| Balance inquiry | Return balance |
| Deposit | Increase account/cash appropriately |
| Session timeout | Session ends |
| Admin takes ATM offline | `OUT_OF_SERVICE` |
| Admin brings ATM online | `IDLE` |

---

# 43. Quick Class Relationship Diagram

```text
                    ┌────────────┐
                    │    ATM     │
                    └─────┬──────┘
                          │ has
                          ▼
                    ┌────────────┐
                    │ ATMState   │
                    └─────┬──────┘
                          │
           ┌──────────────┼─────────────────┐
           ▼              ▼                 ▼
       IdleState   CardInsertedState   AuthenticatedState
                                             │
                                             ▼
                                  TransactionSelectedState
                                             │
                                             ▼
                                  TransactionCompletedState


ATM ─────── has ───────> Session
Session ──── refers to ─> Card
Session ──── refers to ─> Account

TransactionService
        │
        ▼
TransactionStrategy
   ├── WithdrawalStrategy
   ├── DepositStrategy
   └── BalanceInquiryStrategy

ATM ─────── uses ───────> CashDrawer
CashDrawer ─────────────> Denomination
```

---

# 44. One-Line Responsibilities for Revision

Memorize these.

```text
ATM
→ Maintains current state and ATM interaction context.

ATMState
→ Defines what the ATM can do in a particular state.

IdleState
→ Waits for a customer and accepts card insertion.

CardInsertedState
→ Handles PIN/authentication stage.

AuthenticatedState
→ Allows transaction selection.

TransactionSelectedState
→ Executes the selected transaction through TransactionService.

TransactionCompletedState
→ Finishes the current operation and moves toward session completion.

OutOfServiceState
→ Rejects normal customer operations.

TransactionStrategy
→ Defines transaction execution behavior.

WithdrawalStrategy
→ Handles withdrawal-specific logic.

DepositStrategy
→ Handles deposit-specific logic.

BalanceInquiryStrategy
→ Handles balance inquiry.

TransactionService
→ Chooses and executes the correct strategy.

SessionService
→ Creates and manages customer sessions.

CardService
→ Validates/authenticates cards.

ATMService
→ Handles ATM availability and ATM-level operations.

AdminService
→ Handles administrative operations such as cash refill/audit.

CashDrawer
→ Tracks ATM note inventory.

Account
→ Tracks customer balance and withdrawal limits.

Card
→ Tracks card state and PIN retry/blocking behavior.

Transaction
→ Records one financial operation.

Repository
→ Abstracts persistence.
```

---

# 45. Interview Mental Model

When given an LLD problem like this, think in this order:

```text
1. Identify actors
2. Identify core entities
3. Identify state transitions
4. Identify varying algorithms
5. Apply State Pattern
6. Apply Strategy Pattern
7. Add repository/service boundaries
8. Define important invariants
9. Think about concurrency
10. Think about failure recovery
11. Think about extensibility
12. Explain trade-offs
```

For an ATM:

```text
Actors
→ Customer
→ Admin
→ Bank/Core Banking

Entities
→ ATM
→ Card
→ Account
→ Session
→ Transaction
→ CashDrawer

State
→ Idle
→ Card Inserted
→ Authenticated
→ Transaction Selected
→ Completed
→ Out of Service

Strategies
→ Withdraw
→ Deposit
→ Balance
```

---

# 46. Strong Final Interview Answer

If asked to summarize your design:

> "The ATM is modeled as a stateful aggregate where the current state determines which operations are valid. I use the State Pattern to model the ATM lifecycle from idle to card insertion, authentication, transaction selection, and completion.
>
> Transaction execution is separated using the Strategy Pattern, with dedicated strategies for withdrawal, deposit, and balance inquiry. This keeps transaction algorithms independently testable and extensible.
>
> The service layer handles application orchestration, repositories abstract persistence, and controllers expose the functionality through APIs.
>
> The biggest production concern is consistency between the bank account and physical ATM cash inventory. A withdrawal must be idempotent, concurrency-safe, and atomic from the business perspective. In production I would add durable persistence, locking/versioning, idempotency keys, secure PIN/HSM integration, DTOs, exception handling, auditing, reconciliation, and hardware abstraction."

---

# 47. Last-Minute Cheat Sheet

## Patterns

```text
State
→ ATM lifecycle

Strategy
→ Transaction algorithm

Repository
→ Persistence abstraction

Dependency Injection
→ Object construction/dependencies
```

## Important objects

```text
ATM
Card
Account
Session
Transaction
CashDrawer
Denomination
```

## States

```text
IDLE
CARD_INSERTED
AUTHENTICATED
TRANSACTION_SELECTED
TRANSACTION_COMPLETED
OUT_OF_SERVICE
```

## Transactions

```text
WITHDRAW
DEPOSIT
BALANCE
```

## Transaction statuses

```text
PENDING
SUCCESS
FAILED
```

## Biggest concerns

```text
Atomicity
Concurrency
Idempotency
Security
Cash consistency
Session timeout
Failure recovery
Auditing
```

## Best interview distinction

```text
State:
"What can I do now?"

Strategy:
"How do I perform this operation?"
```

---

# 48. 30-Second Pre-Interview Revision

Read this immediately before the interview:

```text
ATM = stateful system

STATE PATTERN
Idle
→ Card Inserted
→ Authenticated
→ Transaction Selected
→ Transaction Completed
→ Idle

STRATEGY PATTERN
Withdrawal
Deposit
Balance

SERVICE LAYER
ATMService
CardService
SessionService
TransactionService
AdminService

REPOSITORIES
ATM
Card
Account
Session
Transaction
CashDrawer
AdminUser

CORE DOMAIN
ATM
Card
Account
Session
Transaction
CashDrawer

MOST IMPORTANT PROBLEM
Withdrawal consistency:
account debit ↔ cash dispense

PRODUCTION ANSWERS
transactions
locking
idempotency
HSM/security
durable persistence
reconciliation
DTOs
@ControllerAdvice
hardware abstraction
observability
```

---

# 49. Final Takeaway

The core design is not really about an ATM.

The reusable LLD lessons are:

```text
Use STATE when behavior changes based on current lifecycle state.

Use STRATEGY when multiple algorithms/behaviors can perform the same kind of operation.

Use REPOSITORY to isolate storage.

Use SERVICE to coordinate business workflows.

Use DEPENDENCY INJECTION to keep components loosely coupled.

When money is involved:
think atomicity + consistency + idempotency + concurrency.

When hardware is involved:
think abstraction + failures + recovery.

When APIs are involved:
think DTOs + validation + exceptions + security.
```

That is the mental model worth carrying into the interview.
