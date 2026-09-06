# Elevator System — LLD Interview Revision Guide

## 60-second answer

> The system separates external requests from internal requests. External requests need **elevator selection**; internal requests need **movement scheduling**. I use the **State Pattern** for elevator operational state and **Strategy Pattern** for both elevator selection and movement. A dispatcher assigns an external request to an elevator and creates a corresponding internal pickup request. Movement then processes destinations using a strategy such as SCAN.

The key production concerns are concurrency, fairness/starvation, safe maintenance, hardware events, and ownership of elevator state.

## Core model

```text
Building
  └── Elevator[]
        ├── currentFloor
        ├── direction
        ├── state
        └── InternalRequest[]

Floor panel → ExternalRequest → Dispatcher → selected Elevator
                                      └→ InternalRequest

InternalRequest → MovementStrategy → next destination
```

## External vs internal request

**External:** customer is waiting on a floor and specifies UP/DOWN. The problem is **which elevator** should serve them.

**Internal:** passenger is already inside an elevator and chooses a destination. The problem is **which floor next**.

## State Pattern

Main elevator states:

```text
STOPPED
MOVING
DOORS_OPENING
DOORS_CLOSING
PRE_MAINTENANCE
MAINTENANCE
```

State answers:

> **“What can this elevator do right now?”**

The elevator delegates state-dependent behavior through `ElevatorStateHandler`. This prevents one giant `if/else` block inside `Elevator`.

## Strategy Pattern

### Elevator selection

```text
ElevatorSelectionStrategy
 ├── NearestElevatorStrategy
 └── LoadBalancingStrategy
```

### Movement

```text
MovementStrategy
 ├── FCFSStrategy
 └── ScanStrategy
```

Strategy answers:

> **“Which algorithm should perform this decision?”**

### Easy distinction

```text
State    = current condition
Strategy = chosen algorithm
```

## Dispatching

Nearest elevator:

```text
abs(currentFloor - requestedFloor)
```

Load balancing prefers the elevator with lower passenger load, with distance used as a tie-breaker in the improved version.

A production dispatcher should consider:

```text
distance
direction compatibility
pending stops
load
ETA
request age
maintenance/fault state
```

**Best optimization target:** expected passenger wait time, not simply minimum floor distance.

## Movement scheduling

FCFS:

```text
serve in request arrival order
```

Simple and fair, but can cause excessive back-and-forth movement.

SCAN:

```text
continue in current direction
serve stops in that direction
reverse when appropriate
```

This resembles how elevator/disk scheduling reduces unnecessary reversals.

## Queue

The dispatcher uses a `BlockingQueue<ExternalRequest>` to decouple request production from dispatching.

```text
Floor panels
   ↓
request queue
   ↓
dispatcher
   ↓
elevator assignment
```

Benefits:

```text
buffering
asynchronous processing
request burst absorption
decoupling
```

## Maintenance

Graceful maintenance is:

```text
busy elevator
    ↓
PRE_MAINTENANCE
    ↓
stop accepting new work
    ↓
finish existing requests
    ↓
MAINTENANCE
```

An idle elevator can enter maintenance directly.

This is preferable to abruptly disabling a car that is currently serving passengers.

## Building state vs elevator state

Building state:

```text
STOPPED → RUNNING → STOPPING → STOPPED
```

Elevator state is per-car:

```text
E1 = MOVING
E2 = STOPPED
E3 = MAINTENANCE
```

Do not collapse these two concepts into one state machine.

## Concurrency — the biggest interview topic

`ConcurrentHashMap` and `BlockingQueue` improve thread safety of individual data structures, but they do **not** make the full workflow atomic.

Potential race:

```text
Thread A checks E1 available
Thread B checks E1 available
Thread A assigns E1
Thread B assigns E1 too
```

Production choices:

```text
atomic compare-and-set assignment
database locking/versioning
distributed lock
single owner/actor per elevator
ordered command stream
```

### Strongest practical design

Give each elevator a single ordered command stream / actor. Only that owner mutates:

```text
currentFloor
direction
state
stop queue
```

This dramatically simplifies correctness.

## Fairness and starvation

A purely optimized dispatcher may repeatedly favor nearby requests. Older requests can starve.

Use **aging**:

```text
priority = basePriority + waitingTimeBonus
```

This trades a tiny amount of optimality for much better fairness.

## Important invariant

A request should have one owner:

```text
PENDING → ASSIGNED → COMPLETED
```

The assignment itself needs atomic ownership in a multi-threaded system.

## Hardware abstraction

A production elevator system should separate software policy from physical hardware:

```text
Elevator
 ├── MotorController
 ├── DoorController
 ├── FloorSensor
 ├── WeightSensor
 ├── EmergencyButton
 └── Display
```

Example:

```java
interface DoorController {
    void open();
    void close();
    boolean isOpen();
}
```

This makes unit testing possible without real hardware.

## Commands vs events

Production systems should distinguish: 

**Commands**
```text
MOVE_TO_FLOOR
OPEN_DOOR
CLOSE_DOOR
ENTER_MAINTENANCE
```

**Events**
```text
FLOOR_REACHED
DOOR_OPENED
DOOR_CLOSED
ELEVATOR_FAULTED
```

The hardware reports what actually happened; business logic should not assume that issuing a command means it succeeded.

## Failure scenarios

### Elevator failure while moving

```text
fault detected
↓
mark unavailable
↓
handle passenger safety
↓
requeue affected requests
↓
maintenance
```

### Scheduler failure

Requests must be durable enough to survive worker restarts. In-memory-only requests are fine for LLD demonstration but not for production.

### Duplicate command

Use request/command IDs and idempotent processing.

## Scaling

For many buildings/elevators:

```text
API Gateway
    ↓
Dispatch / Command Service
    ↓
partition by buildingId
    ↓
ordered stream per elevator
```

Partitioning by building/elevator preserves local ordering and avoids a giant global lock.

## Production improvements

- Separate movement state, service availability, and safety state rather than one giant enum.
- Use real sensor events rather than directly setting floors.
- Add emergency/fault state.
- Add weight-based capacity.
- Persist requests and state.
- Make assignment atomic/idempotent.
- Optimize dispatch on ETA.
- Add request aging for fairness.
- Add metrics such as wait time, travel time, queue depth, utilization, and fault rate.
- Add structured logging and correlation IDs.
- Add global API exception handling and DTOs.

## Likely interview questions

### Why State Pattern?
Because behavior changes with the elevator’s current lifecycle state. It localizes state-specific rules.

### Why two Strategy Patterns?
Because elevator **selection** and **movement ordering** are different decisions.

### Why queue external requests?
To buffer bursts and decouple request creation from asynchronous dispatch.

### Why not only use nearest elevator?
Nearest can be moving in the wrong direction, heavily loaded, or carrying many pending stops. ETA is a better production signal.

### How do you prevent duplicate assignment?
Atomic ownership/assignment, database versioning, or single-owner command processing.

### How do you prevent starvation?
Request aging / maximum wait thresholds.

### How do you handle maintenance?
Use pre-maintenance to stop new work and finish current safe work first.

### What if an elevator fails?
Move it to fault/unavailable, recover passenger safely, and reassign affected requests where possible.

### How would you scale to 1,000 elevators?
Partition by building/elevator and use ordered command streams or actors so each elevator has one logical owner.

## Test cases

### Selection

```text
nearest elevator selected
least-loaded elevator selected
full elevator ignored
maintenance elevator ignored
no elevator available → queued
```

### Movement

```text
SCAN UP
SCAN DOWN
FCFS
duplicate destination floors
already at destination
empty request list
```

### Maintenance

```text
idle → maintenance
busy → pre-maintenance
new requests blocked
existing requests completed
then maintenance
```

### Concurrency

```text
two threads target same elevator
request assigned only once
request completes only once
```

## Class responsibility cheat sheet

```text
Building
→ floor range + building/system lifecycle

Elevator
→ current physical/logical state

ExternalRequest
→ floor-panel request

InternalRequest
→ passenger destination / assigned pickup

DispatcherService
→ external request assignment

MovementService
→ destination scheduling + movement processing

ElevatorService
→ elevator lifecycle/maintenance/persistence coordination

RequestService
→ request persistence and status updates

SelectionStrategy
→ which elevator?

MovementStrategy
→ which floor next?
```

## Last-minute cheat sheet

```text
EXTERNAL REQUEST
→ Which elevator?

INTERNAL REQUEST
→ Which destination next?

STATE
→ What can the elevator do now?

STRATEGY
→ Which algorithm should we use?

DISPATCH
→ Optimize ETA, not just distance

QUEUE
→ Buffer asynchronous requests

CONCURRENCY
→ Assignment + elevator state must have one logical owner

FAIRNESS
→ Aging prevents starvation

MAINTENANCE
→ Finish safe existing work before taking elevator offline

PRODUCTION
→ hardware abstraction + events + persistence + idempotency + observability
```

## Strong final answer

> “The key separation is between dispatching and movement. A floor request asks which elevator should serve it, while an in-elevator request asks which destination the car should serve next. I use State Pattern for elevator lifecycle and Strategy Pattern for selection and movement algorithms. External requests are queued and converted into assigned internal pickup requests. For production, I would make assignment atomic, give each elevator a single ordered command owner, optimize on ETA rather than pure distance, add request aging for fairness, and model hardware commands separately from sensor events.”
