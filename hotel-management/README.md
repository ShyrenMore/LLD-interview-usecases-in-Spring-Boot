# Hotel Management System — LLD Interview Revision Guide

This is an interview-oriented revision guide for the Hotel Management System LLD.

The design models:

- hotel and room-type management
- hotel search
- nightly pricing
- seasonal pricing
- room inventory / availability
- booking lifecycle
- temporary booking holds
- payment transactions and callbacks
- cancellation policies and refunds
- check-in / check-out
- user booking dashboard

The Spring Boot version uses:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
In-memory storage
```

The core LLD ideas are more important than the Spring annotations.

---

# 1. 60-Second Interview Explanation

> "I model the hotel system around Hotel, RoomType, Room, Booking, User, Transaction and pricing/inventory concepts.
>
> The key challenge is that availability is date based, pricing can vary by night, and bookings have a lifecycle. A room type can be available for one night and unavailable for another.
>
> I use a booking state transition component to control valid transitions such as CREATED → HELD → CONFIRMED → CHECKED_IN → CHECKED_OUT, with cancellation paths where appropriate.
>
> Availability is calculated from total inventory minus active reservations/holds/check-ins, with optional overbooking.
>
> Pricing is separated into a PricingService so base pricing and seasonal pricing can evolve independently.
>
> Payment is handled separately from booking using Transaction and a payment callback flow. The important consistency problem is that payment, inventory hold, booking status and refund must remain consistent even when callbacks are retried or requests fail."

---

# 2. Core Domain Model

```text
Hotel
 │
 ├── RoomType
 │      │
 │      └── Rooms
 │
 └── CancellationPolicy

User
 │
 └── Booking
        │
        ├── RoomType
        ├── DateRange
        ├── NightlyPrice[]
        └── Transaction

SeasonalPrice
    │
    └── RoomType + Date

SearchFilter
    ↓
SearchService
    ↓
RoomTypeAvailability
```

---

# 3. Main Entities

## Hotel

Represents the property.

Important fields:

```text
id
name
address
city
country
latitude
longitude
rating
active
defaultOverbookPercent
cancellationPolicyId
createdAt
```

A hotel owns room types.

---

## RoomType

Examples:

```text
Deluxe King
Standard Queen
Suite
```

Important fields:

```text
capacity
bedType
basePriceMinor
amenities
totalRooms
active
```

`totalRooms` is inventory capacity for that room type.

---

## Room

A physical room.

Example:

```text
Hotel: Grand Hotel
Room Type: Deluxe King
Room Number: 101
```

A key distinction:

```text
RoomType = product/inventory category

Room = physical unit
```

This distinction is important.

You usually sell availability at the room-type level and allocate a physical room during check-in.

---

## User

Customer/account holder.

```text
id
name
email
role
createdAt
```

---

## Booking

Represents a reservation request.

Important fields:

```text
bookingStatus
paymentStatus
checkInDate
checkOutDate
nightlyPrices
totalAmount
allocatedRoomId
holdExpiresAt
```

A booking starts as:

```text
CREATED
```

and can move through the reservation lifecycle.

---

## Transaction

Represents payment-related activity.

Important fields:

```text
bookingId
amountMinor
currency
status
providerRef
createdAt
completedAt
refundedAt
```

Payment status:

```text
PENDING
COMPLETED
FAILED
REFUNDED
```

---

# 4. Booking State Machine

This is one of the most important parts of the LLD.

```text
                       ┌──────────────┐
                       │    CREATED   │
                       └──────┬───────┘
                              │ initiate payment
                              ▼
                       ┌──────────────┐
                       │     HELD     │
                       └──────┬───────┘
                              │ payment success
                              ▼
                       ┌──────────────┐
                       │  CONFIRMED   │
                       └──────┬───────┘
                              │ check-in
                              ▼
                       ┌──────────────┐
                       │ CHECKED_IN   │
                       └──────┬───────┘
                              │ check-out
                              ▼
                       ┌──────────────┐
                       │ CHECKED_OUT  │
                       └──────────────┘

CREATED ───────────────→ CANCELLED
HELD ──────────────────→ CANCELLED
CONFIRMED ──────────────→ CANCELLED
```

---

# 5. Why a Booking State Handler?

Without centralized state validation:

```java
if (booking.getStatus() == ...)
```

would appear throughout every service.

Instead:

```java
BookingStateHandler.transition(...)
BookingStateHandler.requireStatus(...)
BookingStateHandler.canCancel(...)
```

This centralizes lifecycle rules.

### Interview answer

> "The state transition component ensures that business state changes remain explicit and invalid transitions cannot be performed accidentally."

---

# 6. State vs State Pattern

The implementation uses a `BookingStateHandler` rather than one class per state.

That is a conscious trade-off.

For this LLD:

```text
BookingStatus enum
      +
BookingStateHandler
```

is simpler than:

```text
CreatedState
HeldState
ConfirmedState
CheckedInState
...
```

For a richer workflow, the full State Pattern could be used.

### Good interview observation

> "I would use enum + transition map when the workflow is mostly status validation. I would move to concrete state objects if each state starts accumulating substantially different behavior."

---

# 7. Valid State Transitions

```text
CREATED
  ├── HELD
  └── CANCELLED

HELD
  ├── CONFIRMED
  └── CANCELLED

CONFIRMED
  ├── CHECKED_IN
  └── CANCELLED

CHECKED_IN
  └── CHECKED_OUT
```

Invalid examples:

```text
CREATED → CHECKED_IN
CANCELLED → CONFIRMED
CHECKED_OUT → CHECKED_IN
```

should be rejected.

---

# 8. Availability — The Core Problem

Availability is not simply:

```text
totalRooms - confirmedBookings
```

The system also accounts for:

```text
HELD bookings
CONFIRMED bookings
CHECKED_IN bookings
```

So:

```text
available
=
totalRooms
+ overbookAllowed
- confirmed
- activeHolds
- checkedIn
```

For every night in the requested date range.

---

# 9. Why Check Every Night?

Suppose:

```text
Room Type = Deluxe King
Total rooms = 10

Jan 10 → 4 booked
Jan 11 → 9 booked
Jan 12 → 2 booked
```

A request for:

```text
Jan 10 → Jan 13
```

is unavailable if the system needs one room every night because:

```text
Jan 11 has only 1 remaining room
```

The availability algorithm therefore iterates:

```text
check-in
   ↓
each night
   ↓
check-out exclusive
```

---

# 10. Date Range Convention

The design uses:

```text
[checkInDate, checkOutDate)
```

meaning:

```text
check-in is included
check-out is excluded
```

Example:

```text
Jan 10 → Jan 12
```

means:

```text
Night Jan 10
Night Jan 11
```

Total:

```text
2 nights
```

This convention avoids many off-by-one errors.

---

# 11. Important Date Handling Point

The current educational implementation treats dates as UTC-aligned millisecond intervals.

In production, prefer a proper date/time model:

```java
LocalDate
ZoneId
Instant
```

Hotel availability is fundamentally calendar-based, not just elapsed milliseconds.

The correct production model should understand the hotel's local timezone.

---

# 12. Overbooking

Hotel may have:

```text
totalRooms = 100
defaultOverbookPercent = 10%
```

Then:

```text
overbookAllowed
= ceil(100 × 10 / 100)
= 10
```

Effective inventory:

```text
110
```

before subtracting bookings.

### Why overbook?

Hotels know that some customers may:

- cancel
- not show
- change dates

But it introduces risk.

A production design may use more sophisticated:

```text
overbooking rules
forecasting
no-show probabilities
room-type-specific limits
date-specific limits
```

---

# 13. Hold vs Confirmed

This is a key booking concept.

## CREATED

The booking record exists but inventory is not yet held.

```text
inventory count unaffected
```

---

## HELD

The user has initiated payment and the room is temporarily reserved.

```text
inventory count decreases
holdExpiresAt = now + TTL
```

This prevents:

```text
Customer A starts payment
Customer B books the same room
```

during the payment window.

---

## CONFIRMED

Payment succeeds.

The hold becomes a permanent reservation.

---

# 14. Hold Expiration

Current hold TTL:

```text
15 minutes
```

Conceptually:

```text
CREATED
   ↓ payment started
HELD
   │
   ├── payment success → CONFIRMED
   │
   └── payment timeout → CANCELLED
```

Production system should process expired holds through:

```text
scheduled job
or
delayed queue
or
lease/TTL mechanism
```

The improved code exposes:

```java
processExpiredHolds()
```

for this purpose.

---

# 15. Booking Flow

```text
Search
  ↓
Check availability
  ↓
Calculate price
  ↓
Create CREATED booking
  ↓
Initiate payment
  ↓
Re-check availability
  ↓
Move to HELD
  ↓
Payment Gateway
  ↓
Callback
  ├── success → CONFIRMED
  └── failure → CANCELLED
```

The second availability check is important.

The first check can become stale because another customer may book in between.

---

# 16. The Double-Booking Race Condition

Imagine:

```text
Room inventory = 1
```

Two requests arrive simultaneously:

```text
Customer A → check availability → 1 available
Customer B → check availability → 1 available
```

Both see availability.

Then both reserve.

Result:

```text
2 bookings
1 room
```

This is a classic race condition.

---

# 17. How to Fix Double Booking

For production:

```text
Database transaction
+
row/version locking
+
atomic inventory reservation
```

Possible approaches:

### Pessimistic locking

```text
SELECT ... FOR UPDATE
```

Serialize reservation operations.

### Optimistic locking

Maintain:

```text
version
```

and update only when version matches.

### Atomic inventory update

Conceptually:

```sql
UPDATE inventory
SET available = available - 1
WHERE room_type_id = ?
  AND date = ?
  AND available >= 1;
```

Then check affected rows.

This can be very effective.

---

# 18. Why Availability Check Alone Is Not Enough

This is a strong interview statement:

> "Availability is a read. Booking is a state-changing operation. I cannot treat a successful availability check as a reservation because the state can change between the two operations."

Therefore:

```text
CHECK
+
RESERVE
```

must be coordinated atomically.

---

# 19. Pricing Design

Pricing uses:

```text
RoomType.basePriceMinor
+
SeasonalPrice
```

Rule:

```text
seasonal price exists
    ↓
use seasonal price

otherwise
    ↓
use base price
```

This means:

```text
RoomType
   ↓
Base price

SeasonalPrice
   ↓
Date-specific override
```

---

# 20. Example Pricing

Base:

```text
$100/night
```

Seasonal price:

```text
Jan 10 = $150
```

Stay:

```text
Jan 10 → Jan 12
```

could become:

```text
Jan 10 → 150
Jan 11 → 100
----------------
Total   → 250
```

The booking stores the resulting nightly prices.

---

# 21. Why Store Nightly Prices in Booking?

Suppose booking occurs today at:

```text
$150/night
```

Later the hotel changes pricing to:

```text
$250/night
```

The existing booking should generally remain at its agreed price.

Therefore:

```text
Booking
  └── nightlyPrices[]
```

acts as a price snapshot.

This is a very important design decision.

---

# 22. Search Service

Responsibilities:

```text
searchHotels()
getAvailability()
```

Search can consider:

```text
city
country
bed type
price range
availability dates
```

The improved version fixes an obvious bug in the original code where the result of the hotel search was immediately replaced with an empty list.

---

# 23. Search vs Availability

Do not confuse:

### Hotel Search

```text
"Which hotels match my filters?"
```

### Availability

```text
"Which room types are actually bookable for these dates?"
```

A hotel may exist and match the city filter but have:

```text
no available rooms
```

for the requested range.

---

# 24. Cancellation Policy

The system models:

```text
refundPercent
cutoffHoursBeforeCheckIn
```

Example:

```text
Refund = 100%
Cutoff = 24 hours
```

Meaning:

```text
Cancel >= 24 hours before check-in
→ 100% refund
```

Earlier than the cutoff is not the same as cancellation any time.

---

# 25. Refund Calculation

Current logic:

```text
refundAmount
=
booking.totalAmount × refundPercent / 100
```

Example:

```text
Booking = $500
Refund = 80%

Refund = $400
```

Money uses minor units in the implementation.

---

# 26. Payment Flow

```text
Booking CREATED
      ↓
Initiate transaction
      ↓
Booking HELD
      ↓
Transaction PENDING
      ↓
Payment Gateway
      ↓
Callback
      ├── COMPLETED
      │      ↓
      │  Booking CONFIRMED
      │
      └── FAILED
             ↓
         Booking CANCELLED
```

---

# 27. Why Payment Is Separate From Booking

Booking and payment are different concerns.

```text
Booking
→ reservation lifecycle

Transaction
→ financial lifecycle
```

This separation makes the system easier to extend for:

```text
different payment providers
retries
refunds
webhooks
reconciliation
multiple payment methods
```

---

# 28. Payment Callback Idempotency

The payment gateway may send the same callback more than once.

Example:

```text
COMPLETED
COMPLETED
COMPLETED
```

The service checks whether the transaction is already completed/failed.

That prevents the same callback from repeatedly changing state.

### Stronger production model

Store:

```text
providerRef
eventId
processedAt
```

with a unique constraint.

---

# 29. Payment Callback Race Conditions

Two different callbacks could theoretically arrive concurrently.

For example:

```text
SUCCESS
FAILED
```

for the same transaction.

A production implementation needs:

```text
transaction row locking
version check
or idempotent state transition
```

so that terminal states cannot be overwritten incorrectly.

---

# 30. Refund Flow

When a paid booking is cancelled:

```text
Cancellation policy
      ↓
RefundDecision
      ↓
Booking CANCELLED
      ↓
Refund transaction
      ↓
Payment Gateway
```

Production should integrate with the real payment provider and store:

```text
refund provider reference
refund status
refund timestamps
retry count
```

---

# 31. Booking vs Physical Room Allocation

The reservation is generally for:

```text
RoomType
```

not a specific room.

Example:

```text
Deluxe King
```

Customer books:

```text
1 Deluxe King
```

At check-in:

```text
Room 101
```

is allocated.

This decouples:

```text
inventory sold
```

from:

```text
physical room assignment
```

---

# 32. Check-In Flow

```text
Booking CONFIRMED
      ↓
Admin selects physical room
      ↓
allocatedRoomId = roomId
      ↓
checkInTimeUtc = now
      ↓
CHECKED_IN
```

A production system should also validate:

```text
room belongs to hotel
room belongs to room type
room is active
room is not already occupied
```

These validations are intentionally lightweight in the LLD.

---

# 33. Check-Out Flow

```text
CHECKED_IN
    ↓
set checkOutTimeUtc
    ↓
CHECKED_OUT
```

After checkout, the booking no longer counts as an active room reservation in the simple inventory model.

---

# 34. Repository Pattern

Repositories isolate storage.

Examples:

```text
HotelRepository
RoomTypeRepository
RoomRepository
BookingRepository
TransactionRepository
SeasonalPriceRepository
UserRepository
CancellationPolicyRepository
```

The sample uses:

```java
ConcurrentHashMap
```

as an in-memory data store.

In production:

```text
Repository
   ↓
JPA / JDBC / Database
```

---

# 35. Why Not Put Everything in One Repository?

Because each aggregate has different access patterns.

Examples:

```text
BookingRepository
→ find by user
→ count by date/status
→ find expired holds

SeasonalPriceRepository
→ lookup by hotel + room type + date

TransactionRepository
→ lookup by provider reference
```

Separate repositories keep those queries cohesive.

---

# 36. Service Responsibilities

## BookingService

```text
create booking
cancel booking
check-in
check-out
expire holds
```

---

## InventoryService

```text
availability calculation
booking counts
overbooking rules
```

---

## PricingService

```text
nightly pricing
seasonal override
total price
average price
```

---

## PolicyService

```text
cancellation rules
refund calculation
```

---

## TransactionService

```text
payment initiation
payment callback
refund
```

---

## SearchService

```text
hotel search
room availability response
```

---

## UserService

```text
customer booking history
```

---

# 37. Spring Boot Architecture

```text
@RestController
       ↓
@Service
       ↓
@Repository
       ↓
ConcurrentHashMap
```

Example:

```text
BookingController
       ↓
BookingService
       ↓
BookingRepository
```

and:

```text
BookingService
   ├── InventoryService
   ├── PricingService
   ├── PolicyService
   ├── TransactionService
   └── Repositories
```

---

# 38. Important Dependency Direction

A useful mental model:

```text
Controller
    ↓
Service
    ↓
Repository
```

Cross-service collaboration is okay when it represents a real business workflow.

Avoid:

```text
Controller → Repository
```

for business operations.

The controller should normally delegate to a service.

---

# 39. Concurrency — The Biggest Production Topic

The in-memory repositories use:

```java
ConcurrentHashMap
```

but this does NOT make booking atomic.

Remember:

```text
Thread-safe map
≠
Thread-safe business workflow
```

The business operation:

```text
check availability
+
create hold
```

must be atomic.

---

# 40. Inventory Reservation Model

A more robust architecture can introduce an explicit reservation:

```text
InventoryReservation
--------------------
reservationId
hotelId
roomTypeId
date
quantity
status
expiresAt
```

Then:

```text
CREATED
   ↓
RESERVED
   ↓
CONFIRMED

or

EXPIRED / RELEASED
```

This makes inventory management a first-class concept.

---

# 41. Strong Production Architecture

```text
                 ┌───────────────┐
                 │ Search API    │
                 └───────┬───────┘
                         │
                         ▼
                 ┌───────────────┐
                 │ SearchService │
                 └───────┬───────┘
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
         Hotel Catalog          Availability
                                  Service
                                     │
                                     ▼
                               Inventory Store
                                     │
                                     ▼
                                Reservation


                 Booking API
                     │
                     ▼
               BookingService
                     │
          ┌──────────┼───────────┐
          ▼          ▼           ▼
      Inventory    Pricing    Payment
       Service     Service     Service
                                  │
                                  ▼
                           Payment Gateway
```

---

# 42. Caching

Search is a strong candidate for caching.

Possible caches:

```text
hotel by city
hotel metadata
room type metadata
static amenities
seasonal pricing
popular availability queries
```

But be careful.

Availability is highly dynamic.

Avoid long-lived caching of:

```text
"1 room available"
```

because it becomes stale quickly.

---

# 43. Database Design — Interview Extension

Possible tables:

```text
users
hotels
room_types
rooms
bookings
booking_nightly_prices
seasonal_prices
cancellation_policies
transactions
inventory_reservations
```

Potential relationships:

```text
hotel
  └── room_type
       └── room

hotel
  └── cancellation_policy

booking
  ├── user
  ├── hotel
  ├── room_type
  └── transactions
```

---

# 44. Why Have `booking_nightly_prices`?

Instead of storing a JSON/list directly in the booking table:

```text
booking_nightly_prices
----------------------
booking_id
date
price_minor
```

This provides:

- auditability
- easy querying
- price history
- better relational modeling

For an LLD interview, either approach is reasonable depending on scope.

---

# 45. Important Database Constraints

Useful production constraints:

```text
booking.id UNIQUE

transaction.provider_ref UNIQUE

seasonal_price
UNIQUE(hotel_id, room_type_id, date)

room.room_number + hotel_id UNIQUE

transaction.id UNIQUE
```

These protect against duplicate records.

---

# 46. Reservation Consistency

The most important invariant:

```text
For every hotel + roomType + date:

activeReservations <= totalRooms + allowedOverbooking
```

where active reservations include:

```text
HELD
CONFIRMED
CHECKED_IN
```

The system must ensure this invariant under concurrency.

---

# 47. Payment Consistency

Another important invariant:

```text
BOOKING CONFIRMED
    implies
PAYMENT COMPLETED
```

Similarly:

```text
BOOKING CANCELLED
    may imply
PAYMENT FAILED / REFUNDED / PENDING
```

depending on the exact failure/cancellation path.

A production state machine should explicitly model those combinations rather than relying only on independent enums.

---

# 48. Saga / Event-Driven Extension

Booking spans multiple systems:

```text
Booking
Inventory
Payment
Notification
```

A distributed transaction across all of them is undesirable.

A Saga can coordinate:

```text
Create booking
    ↓
Reserve inventory
    ↓
Charge payment
    ↓
Confirm booking
```

Failure:

```text
payment failed
    ↓
release inventory
    ↓
cancel booking
```

This is a natural architecture extension when the system becomes distributed.

---

# 49. Outbox Pattern

Suppose booking confirmation succeeds but event publishing fails.

```text
DB update = SUCCESS
Kafka publish = FAILED
```

Now downstream systems don't know the booking was confirmed.

Use:

```text
Booking DB transaction
+
Outbox record
```

Then a publisher reliably sends:

```text
BOOKING_CONFIRMED
```

This is an advanced but excellent interview discussion.

---

# 50. Notifications

After confirmation:

```text
BOOKING_CONFIRMED
```

can trigger:

```text
Email
SMS
Push notification
```

Keep notification delivery asynchronous.

Don't make the customer request wait for:

```text
email provider
```

to finish.

---

# 51. Search Scalability

Hotel search is read-heavy.

A production system could use:

```text
Search index
```

such as Elasticsearch/OpenSearch for:

```text
city
country
rating
amenities
bed type
price
```

Availability should still be sourced from an authoritative inventory/reservation system.

---

# 52. Failure Scenarios

Be ready for these:

### Payment timeout

```text
Booking HELD
Payment unknown
```

Do not immediately mark failed if the payment gateway may still succeed.

Use:

```text
pending reconciliation
```

---

### Payment success after hold expiry

This is tricky.

Possible flow:

```text
HELD expires
→ booking cancelled
→ payment SUCCESS arrives
```

Production policy must decide whether to:

```text
re-confirm if inventory still exists
or
auto-refund
```

This requires an explicit business rule.

---

### Duplicate payment callback

Use idempotency.

---

### Customer retries booking

Use an idempotency key on create/hold operations if needed.

---

### Database outage

Booking should fail safely rather than pretending to reserve inventory.

---

# 53. Idempotency

Important operations:

```text
create booking
initiate payment
payment callback
refund
```

should support idempotency.

Example:

```text
Idempotency-Key: ABC123
```

Store:

```text
ABC123 → booking response
```

A retry should not create another booking.

---

# 54. Security

Production system should address:

```text
authentication
authorization
PII protection
payment tokenization
rate limiting
audit logging
encryption
fraud detection
```

Never store raw card information.

Use the payment provider's tokenization mechanisms.

---

# 55. Money

The implementation uses:

```text
long ...Minor
```

Examples:

```text
priceMinor
totalAmountMinor
refundAmountMinor
```

This avoids floating-point money calculations.

For Java applications, another strong choice is:

```java
BigDecimal
```

when decimal arithmetic is required.

---

# 56. Time

The source model uses UTC timestamps.

A production design should distinguish:

```text
Instant
LocalDate
ZoneId
```

Example:

```text
Booking dates
→ LocalDate in hotel's timezone

Payment event timestamp
→ Instant

Audit event
→ Instant
```

This avoids daylight-saving and timezone errors.

---

# 57. Improved Code Compared With the Original

The submitted code had several issues that were cleaned up:

### Search bug

The original effectively did:

```java
hotels = hotelRepository.findByLocation(...);
...
hotels = new ArrayList<>();
```

which erased the search result.

The improved version preserves the result.

---

### Repository naming

Original:

```text
BookingRepositoryImpl
HotelRepositoryImpl
...
```

Improved Spring style:

```text
BookingRepository
HotelRepository
...
```

with `@Repository`.

---

### Dependency injection

Original manually created dependencies.

Spring version uses:

```java
@Service
@RequiredArgsConstructor
```

and constructor injection.

---

### Logging

Simulation uses SLF4J:

```java
log.info(...)
log.warn(...)
```

instead of:

```java
System.out.println(...)
```

---

### Expired holds

The original only had commented-out scheduler logic.

The improved version adds:

```java
processExpiredHolds()
```

which can later be scheduled using:

```java
@Scheduled(...)
```

---

### Validation

The improved booking flow validates:

```text
user exists
hotel exists
room type exists
room type belongs to hotel
hotel active
room type active
date range valid
availability
price mismatch
```

---

# 58. Why `CREATED` Does Not Hold Inventory

This is an intentional design choice.

```text
create booking
```

does not automatically reserve inventory.

Only when payment begins:

```text
CREATED → HELD
```

does the booking count against inventory.

This reduces inventory being unnecessarily blocked by abandoned carts.

Alternative design:

```text
create booking → HELD immediately
```

is also valid.

The interviewer mainly cares that you can explain the trade-off.

---

# 59. Booking Hold TTL Trade-off

Too short:

```text
5 min
```

customers may fail to complete payment.

Too long:

```text
60 min
```

inventory can be blocked unnecessarily.

15 minutes is a reasonable LLD example, but the actual value should be based on:

```text
payment completion latency
conversion metrics
inventory scarcity
business rules
```

---

# 60. Search Price Filter Caveat

The simplified search implementation uses base room-type pricing for filtering.

Production search may need to consider:

```text
actual requested dates
seasonal prices
fees
taxes
promotions
currency
occupancy
```

Therefore:

```text
"starting from $100"
```

and:

```text
"exact price for my dates"
```

are different queries.

---

# 61. Fees and Taxes

Real hotel pricing often looks like:

```text
base room rate
+ taxes
+ service fees
+ resort fees
- discounts
= final payable amount
```

A scalable design might introduce:

```text
PricingRule
TaxRule
PromotionStrategy
FeeCalculator
```

This is a natural future Strategy Pattern use case.

---

# 62. Loyalty / Promotions

If pricing gets more complex:

```text
PromotionStrategy
    ├── SeasonalPromotion
    ├── CouponPromotion
    ├── LoyaltyDiscount
    └── MemberRate
```

Then:

```text
PricingService
```

can compose multiple pricing rules.

---

# 63. Questions Interviewers Are Likely to Ask

## Q1. Why room type instead of room?

Because customers typically reserve a room category rather than a physical room, and the physical unit can be assigned later.

---

## Q2. How do you prevent double booking?

Availability check alone is not enough. Use atomic reservation / database locking / optimistic locking.

---

## Q3. Why keep nightly prices in Booking?

To preserve the agreed price even if pricing changes later.

---

## Q4. Why use HELD?

To temporarily reserve inventory during payment.

---

## Q5. What happens when a payment callback comes twice?

Idempotency prevents duplicate state transitions.

---

## Q6. What if payment succeeds after the hold expires?

Explicit reconciliation policy is required; likely re-check inventory and either confirm or refund.

---

## Q7. Why separate payment transaction from booking?

They represent different lifecycles and integrate with external payment systems independently.

---

## Q8. Why `ConcurrentHashMap`?

For concurrent in-memory repository access, but it does not solve atomic booking workflows.

---

## Q9. Why not assign a room at booking time?

Because the product being sold is generally a room type; physical room assignment can be delayed until check-in.

---

## Q10. How would you scale search?

Use a search index/cache for hotel/catalog queries and keep authoritative inventory elsewhere.

---

# 64. Advanced Interview Questions

### "Would you use microservices?"

Do not say yes automatically.

Start with a modular monolith:

```text
Search
Booking
Inventory
Pricing
Payment
Notification
```

Then split services when:

```text
scale
ownership
deployment independence
failure isolation
```

justify it.

---

### "Would you use Kafka?"

Only when asynchronous communication and event-driven integration justify it.

Examples:

```text
BOOKING_CONFIRMED
PAYMENT_COMPLETED
BOOKING_CANCELLED
REFUND_COMPLETED
```

---

### "Would you cache availability?"

Only carefully.

Availability changes frequently, so stale data can produce poor booking decisions.

Cache catalog/static data aggressively; treat booking inventory as authoritative.

---

# 65. Testing Strategy

## Booking tests

```text
create booking successfully
invalid user
invalid hotel
invalid room type
room type belongs to different hotel
inactive hotel
inactive room type
invalid date range
no availability
price mismatch
```

---

## Availability tests

```text
one room available
no rooms available
held booking consumes inventory
confirmed booking consumes inventory
checked-in booking consumes inventory
expired hold does not consume inventory
overbooking enabled
overbooking disabled
multi-night request with one unavailable night
```

---

## Payment tests

```text
initiate payment
success callback
failure callback
duplicate success callback
unknown provider reference
callback for invalid booking state
refund
```

---

## State transition tests

```text
CREATED → HELD
HELD → CONFIRMED
HELD → CANCELLED
CONFIRMED → CHECKED_IN
CHECKED_IN → CHECKED_OUT
CONFIRMED → CANCELLED

invalid:
CREATED → CHECKED_IN
CHECKED_OUT → CONFIRMED
CANCELLED → CHECKED_IN
```

---

# 66. Testing the Double-Booking Race

This is an advanced test.

Start with:

```text
1 available room
```

Run two concurrent booking requests.

Expected:

```text
1 succeeds
1 fails
```

Never:

```text
2 succeed
```

This is one of the most valuable concurrency tests for the system.

---

# 67. SOLID Principles

## Single Responsibility

Separate:

```text
pricing
inventory
booking
payment
policy
search
```

---

## Open/Closed

Pricing and promotion logic can evolve without modifying booking lifecycle logic.

---

## Dependency Inversion

Services depend on repository abstractions/concepts.

Spring handles construction.

---

# 68. Strategy Pattern Opportunities

The current code doesn't need Strategy everywhere.

Future good candidates:

```text
PricingStrategy
CancellationStrategy
RoomAllocationStrategy
PaymentProviderStrategy
PromotionStrategy
```

Example:

```text
PaymentProvider
   ├── StripePaymentProvider
   ├── RazorpayPaymentProvider
   └── AdyenPaymentProvider
```

This is preferable to:

```java
if (provider.equals("STRIPE")) ...
else if (provider.equals("RAZORPAY")) ...
```

throughout the codebase.

---

# 69. Factory Pattern Opportunities

A factory could create:

```text
PaymentProvider
PromotionStrategy
RoomAllocationStrategy
```

based on configuration.

For example:

```text
PaymentProviderFactory
       ↓
STRIPE → StripePaymentProvider
RAZORPAY → RazorpayPaymentProvider
```

Use Factory when object creation itself needs selection logic.

---

# 70. State vs Strategy in This LLD

### State

```text
BookingStatus
```

answers:

> "What lifecycle stage is this booking in?"

### Strategy

Potential future examples:

```text
PricingStrategy
CancellationStrategy
PaymentProvider
```

answer:

> "Which behavior/algorithm should perform this operation?"

---

# 71. Most Important Invariants

Memorize these.

```text
1. check-in < check-out

2. booking dates use [check-in, check-out)

3. room type belongs to hotel

4. inactive hotel/room type cannot be booked

5. active inventory cannot be exceeded
   after accounting for overbooking policy

6. HELD bookings have an expiry

7. payment success should not confirm an invalid booking state

8. confirmed booking should normally have completed payment

9. cancellation cannot happen after checkout

10. transaction provider reference should be unique

11. nightly booking price is a snapshot

12. duplicate payment callbacks must be idempotent
```

---

# 72. If Asked to Design This From Scratch

Go in this order:

```text
1. Clarify scope
2. Identify entities
3. Separate room type from physical room
4. Define booking lifecycle
5. Define availability model
6. Define pricing
7. Define payment
8. Define cancellation/refund
9. Add repository/service boundaries
10. Discuss concurrency
11. Discuss idempotency
12. Discuss scaling
```

---

# 73. Best High-Level Diagram to Draw

```text
                  ┌──────────────┐
                  │    Client    │
                  └──────┬───────┘
                         │
              ┌──────────┴───────────┐
              ▼                      ▼
        SearchController      BookingController
              │                      │
              ▼                      ▼
        SearchService          BookingService
              │                 /     |      \
              │                /      |       \
              ▼               ▼       ▼        ▼
        HotelRepository   Inventory Pricing Transaction
        RoomTypeRepo        Service  Service   Service
                                  │       │        │
                                  ▼       ▼        ▼
                             Booking   Pricing   Payment
                              Store     Store    Gateway
```

---

# 74. Last-Minute Cheat Sheet

## Entities

```text
Hotel
RoomType
Room
User
Booking
Transaction
SeasonalPrice
CancellationPolicy
```

## Booking states

```text
CREATED
HELD
CONFIRMED
CHECKED_IN
CHECKED_OUT
CANCELLED
```

## Payment states

```text
PENDING
COMPLETED
FAILED
REFUNDED
```

## Services

```text
SearchService
InventoryService
PricingService
BookingService
TransactionService
PolicyService
UserService
```

## Core formulas

```text
available rooms
=
total rooms
+ overbook
- held
- confirmed
- checked-in
```

```text
total price
=
sum(nightly prices)
```

```text
refund
=
booking total × refund %
```

---

# 75. The Three Hardest Problems

If you remember only three things:

### 1. Double booking

```text
availability read
≠
reservation guarantee
```

Need atomic reserve.

### 2. Payment consistency

```text
payment
↔
booking
↔
inventory
```

Need idempotency/reconciliation.

### 3. Price consistency

```text
current pricing
≠
historical booking price
```

Store the nightly price snapshot.

---

# 76. Strong Final Interview Answer

> "The core model separates hotel inventory from physical room allocation. Customers reserve a RoomType for a date range, while a physical Room can be assigned at check-in.
>
> Availability is calculated per night as total inventory plus allowed overbooking minus active HELD, CONFIRMED and CHECKED_IN reservations. A booking starts in CREATED, moves to HELD when payment begins, becomes CONFIRMED after a successful payment callback, and then moves through CHECKED_IN and CHECKED_OUT.
>
> Pricing is separated into a PricingService with base and seasonal prices, and the calculated nightly price is snapshotted into the booking so later price changes don't affect an existing reservation.
>
> Payment is modeled independently using Transaction. The system must be idempotent because payment callbacks can be retried.
>
> The biggest production concern is preventing double booking. A simple availability check is not sufficient; the inventory reservation must be concurrency-safe using transactions, locking, optimistic versioning or atomic database updates. In a distributed architecture I would consider a reservation service, idempotency keys, outbox/events, reconciliation, and asynchronous notifications."

---

# 77. Final Mental Model

```text
HOTEL MANAGEMENT SYSTEM

Catalog
→ Hotel
→ RoomType
→ Room

Search
→ SearchFilter
→ SearchService

Inventory
→ totalRooms
→ HELD
→ CONFIRMED
→ CHECKED_IN
→ overbooking

Pricing
→ base price
→ seasonal price
→ nightly snapshot

Booking
→ CREATED
→ HELD
→ CONFIRMED
→ CHECKED_IN
→ CHECKED_OUT
→ CANCELLED

Payment
→ PENDING
→ COMPLETED / FAILED
→ REFUNDED

Cancellation
→ policy
→ refund decision

Production
→ concurrency
→ atomic reservation
→ idempotency
→ reconciliation
→ caching
→ events
```

The core lesson:

```text
SEARCH tells you what appears available.
RESERVATION guarantees inventory.
PAYMENT confirms financial state.
BOOKING ties the business lifecycle together.
```
