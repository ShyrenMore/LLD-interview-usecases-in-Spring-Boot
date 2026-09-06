# Music Streaming LLD --- Interview Revision Guide

> **Goal:** Use this README for a 5--10 minute revision immediately
> before an LLD interview.
>
> **Package:** `com.personal.lld`
>
> **Stack:** Java 17, Spring Boot, Lombok, SLF4J, in-memory repositories

------------------------------------------------------------------------

# 1. Problem in One Minute

We are designing a simplified music streaming platform similar to
Spotify.

Core capabilities:

-   Users and subscription tiers
-   Artists, albums and songs
-   Search
-   Playback sessions
-   Play / pause / resume / seek
-   Next / previous
-   Shuffle and repeat
-   Listening history
-   Playlists
-   Premium downloads
-   Chunk-based audio streaming
-   Streaming cache
-   Recommendations
-   Concurrency control for playlist updates

The important interview discussion is not the CRUD itself. Focus on:

1.  **Playback state management**
2.  **Streaming protocol**
3.  **Caching**
4.  **Concurrency**
5.  **Recommendation Strategy Pattern**
6.  **Subscription/authorization**
7.  **How the in-memory LLD evolves into a production system**

------------------------------------------------------------------------

# 2. High-Level Architecture

``` text
                    ┌──────────────────────┐
                    │       Client         │
                    │ Mobile / Web / TV     │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    REST Controllers  │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │       Services       │
                    │                      │
                    │ Playback              │
                    │ Streaming             │
                    │ Playlist              │
                    │ Download              │
                    │ Search                │
                    │ Recommendation       │
                    └──────┬───────┬───────┘
                           │       │
                ┌──────────┘       └───────────┐
                ▼                              ▼
       ┌─────────────────┐             ┌───────────────┐
       │  Repositories   │             │    Cache      │
       │ In-memory here  │             │ Audio chunks  │
       └─────────────────┘             └───────────────┘
```

### Layer responsibilities

  Layer         Responsibility
  ------------- ---------------------------------------
  Controller    HTTP/API boundary
  Service       Business logic and orchestration
  Repository    Persistence abstraction
  Domain        Entities and enums
  Strategy      Pluggable recommendation algorithms
  Cache         Frequently accessed streaming chunks
  LockService   Concurrent playlist update protection
  Simulator     Demonstrates end-to-end usage

------------------------------------------------------------------------

# 3. Important Domain Objects

## User

``` text
User
├── id
├── username
├── email
├── name
├── subscriptionTier
└── createdAt
```

Subscription:

``` text
FREE
PREMIUM
```

Premium users can access features such as downloads.

------------------------------------------------------------------------

## Artist

``` text
Artist
├── id
├── artistId
├── name
├── thumbnailUrl
└── createdAt
```

------------------------------------------------------------------------

## Album

``` text
Album
├── id
├── albumId
├── title
├── artistId
├── thumbnailUrl
└── createdAt
```

------------------------------------------------------------------------

## Song

``` text
Song
├── id
├── songId
├── title
├── artistId
├── albumId
├── duration
├── genre
├── audioUrl
├── thumbnailUrl
├── fileSize
├── quality
├── format
└── createdAt
```

Audio quality:

``` text
STANDARD → 128 kbps
HIGH     → 256 kbps
PREMIUM  → 320 kbps
```

Audio format:

``` text
MP3
AAC
WAV
```

------------------------------------------------------------------------

# 4. Playback Session --- Most Important Part

A playback session represents the current state of a user's music
player.

``` text
PlaybackSession
├── sessionId
├── userId
├── currentSongId
├── currentPosition
├── playbackSource
├── sourceId
├── queue
├── shuffleMode
├── repeatMode
├── status
├── deviceId
├── startedAt
└── lastUpdatedAt
```

Playback source:

``` text
SONG
ALBUM
PLAYLIST
```

Playback status:

``` text
PLAYING
PAUSED
STOPPED
```

Repeat mode:

``` text
OFF
ONE
```

### Important interview point

The supplied LLD keeps **one playback session per user**.

In production, this should generally become:

``` text
User + Device → PlaybackSession
```

because the same user can be logged in on a phone, laptop and TV
simultaneously.

------------------------------------------------------------------------

# 5. Playback Flow

## Play

``` text
Client
  │
  │ play(userId, sourceType, sourceId)
  ▼
PlaybackController
  │
  ▼
PlaybackService
  │
  ├── Validate user
  ├── Build queue
  ├── Create/update PlaybackSession
  ├── Set current song
  ├── Set status = PLAYING
  └── Get stream URL
```

Possible queues:

### Song

``` text
Song A
```

### Album

``` text
Song A → Song B → Song C → Song D
```

### Playlist

``` text
Song X → Song Y → Song Z
```

------------------------------------------------------------------------

# 6. Next / Previous

### Next

``` text
Current song
     │
     ▼
Save listening history
     │
     ▼
Check repeat mode
     │
     ├── ONE → same song
     │
     └── OFF → next queue item
                 │
                 ├── exists → play next
                 └── absent  → STOP
```

### Previous

Find the previous song in the playback queue.

Production systems often make previous-button behavior more
sophisticated, for example:

-   If current position is \> a threshold, restart current song.
-   Otherwise move to previous song.
-   Consider actual playback history rather than only queue position.

------------------------------------------------------------------------

# 7. Listening History

A listening history record contains:

``` text
ListeningHistory
├── userId
├── songId
├── playedAt
├── playDuration
└── completed
```

The service uses a completion threshold of approximately:

``` text
90% of song duration
```

Example:

``` text
Song duration = 200 sec
Completion threshold = 180 sec
```

If the user reaches 180 seconds, the song can be considered completed.

### Same-day upsert

The LLD avoids creating unlimited history records for repeated progress
updates.

Conceptually:

``` text
(userId + songId + same day)
          ↓
   find existing record
          ↓
 update it instead of inserting another
```

### Production consideration

"Same day" should use a well-defined business timezone. The simplified
implementation uses an epoch-day approach; production systems should
explicitly define timezone semantics.

------------------------------------------------------------------------

# 8. Streaming Protocols

Audio streaming needs to deliver data progressively so playback can
begin before the entire file has downloaded.

There are three important approaches.

------------------------------------------------------------------------

## Approach 1 --- HTTP Range Requests

The client requests a specific byte range of an audio file.

Example:

``` http
GET /api/stream/song123
Range: bytes=0-1048575
```

Server:

``` http
HTTP/1.1 206 Partial Content
Content-Range: bytes 0-1048575/5242880
Accept-Ranges: bytes
```

Body:

``` text
First 1 MB of audio
```

The client can then request:

``` text
1048576 - 2097151
2097152 - 3145727
...
```

### Flow

``` text
Client
  │
  │ Range: bytes=0-1MB
  ▼
Streaming API
  │
  ▼
Cache
  │
  ├── HIT  → return chunk
  │
  └── MISS → read from storage
                │
                ▼
              Cache
                │
                ▼
            return chunk
```

### Benefits

-   Simple
-   Standard HTTP
-   Works with normal web infrastructure/CDNs
-   Supports seeking
-   Good for on-demand streaming
-   No special client library required

### Limitations

-   Client manages buffering
-   No built-in adaptive quality
-   Audio needs to be available as a file/object that supports ranges

------------------------------------------------------------------------

# 9. HLS --- HTTP Live Streaming

HLS divides media into small segments and provides an `.m3u8` playlist.

Example:

``` text
playlist.m3u8
   │
   ├── segment1
   ├── segment2
   ├── segment3
   └── ...
```

Example playlist concept:

``` text
#EXTM3U
#EXTINF:10.0, segment1
#EXTINF:10.0, segment2
#EXTINF:10.0, segment3
```

### Advantages

-   Adaptive bitrate support
-   Good error recovery
-   Works for live and on-demand content
-   Standardized

### Disadvantages

-   More infrastructure
-   Requires media segmentation
-   Playlist/segment management adds complexity

------------------------------------------------------------------------

# 10. DASH

DASH is another adaptive streaming protocol.

Conceptually:

``` text
Client
  │
  ▼
Manifest
  │
  ├── 128 kbps
  ├── 256 kbps
  └── 320 kbps
       │
       ▼
   Media segments
```

It is flexible and supports multiple tracks and quality levels, but is
more complex than the interview-level solution.

------------------------------------------------------------------------

# 11. Why HTTP Range Requests?

For this LLD, choose:

> **HTTP Range Requests**

Reasons:

-   Simple to explain
-   Standard HTTP feature
-   Supports progressive playback
-   Supports seeking
-   Easy to cache
-   Easy to implement in an LLD
-   Demonstrates understanding of HTTP without introducing unnecessary
    media infrastructure

A strong interview answer:

> "For the LLD I would use HTTP byte-range requests. The client requests
> audio chunks using the Range header, the server responds with 206
> Partial Content and Content-Range, and chunks can be cached
> independently. In production, if adaptive bitrate or live streaming
> becomes a requirement, I would consider HLS or DASH."

------------------------------------------------------------------------

# 12. Streaming Implementation Details

### Chunk size

``` text
1 MB = 1,048,576 bytes
```

Configurable rather than hard-coded in a production implementation.

### Cache key

``` text
chunk_{songId}_{start}_{end}
```

Example:

``` text
chunk_song123_0_1048575
```

### HTTP headers

Important headers:

``` http
Accept-Ranges: bytes
Content-Range: bytes 0-1048575/5242880
```

Successful partial response:

``` http
206 Partial Content
```

### Client behavior

The client can:

``` text
Request chunk 1
     │
     ├───────────────► Play chunk 1
     │
Request chunk 2
Request chunk 3
     │
     ▼
Prefetch while playing
```

Sequential or parallel requests can be used for buffering.

------------------------------------------------------------------------

# 13. Streaming Controller

Conceptually:

``` text
GET /api/stream/{songId}
```

Parameters:

``` text
start
end
userId
```

The LLD's streaming implementation returns mock chunk data rather than
reading real audio files.

### Production API

A production implementation should return:

``` text
HTTP 206
Content-Type: audio/mpeg
Accept-Ranges: bytes
Content-Range: bytes start-end/total
Content-Length: chunkSize
```

It would typically read the bytes from object storage/CDN rather than
the application JVM.

------------------------------------------------------------------------

# 14. Cache Design

`CacheService` caches streaming chunks.

Key:

``` text
songId + start + end
```

The implementation uses a concurrent map and an LRU-style eviction
policy.

Maximum entries:

``` text
1000
```

When the cache is full:

``` text
New chunk
   │
   ▼
Cache full?
   │
  YES
   │
   ▼
Find least recently accessed
   │
   ▼
Evict
   │
   ▼
Insert new chunk
```

### Cache hit

``` text
Request
  │
  ▼
Cache
  │
  └── HIT → return immediately
```

### Cache miss

``` text
Request
  │
  ▼
Cache
  │
  └── MISS
       │
       ▼
    Storage
       │
       ▼
     Cache
       │
       ▼
    Client
```

### Production improvements

-   Redis for distributed/shared caching
-   CDN edge caching
-   Object storage such as S3
-   Cache TTL
-   Maximum byte size rather than only entry count
-   Metrics: hit rate, miss rate, eviction rate

------------------------------------------------------------------------

# 15. Repository Pattern

Repositories hide persistence details.

Examples:

``` text
SongRepository
ArtistRepository
AlbumRepository
UserRepository
PlaylistRepository
DownloadRepository
ListeningHistoryRepository
PlaybackSessionRepository
```

The service depends on:

``` java
SongRepository
```

instead of:

``` java
ConcurrentHashMap
```

This is useful because persistence can later change:

``` text
In-memory
   ↓
Database
   ↓
Distributed database
```

without rewriting business logic.

### Interview phrase

> "Repository abstraction separates business logic from persistence."

------------------------------------------------------------------------

# 16. Strategy Pattern --- Recommendations

Recommendation algorithms can change.

Use:

``` java
interface RecommendationStrategy
```

Implementations:

``` text
GenreBasedStrategy
PopularityBasedStrategy
CollaborativeFilteringStrategy
```

Flow:

``` text
RecommendationService
        │
        ▼
RecommendationStrategy
        │
        ├── GenreBased
        ├── PopularityBased
        └── CollaborativeFiltering
```

### Why Strategy?

Without Strategy:

``` text
if genre...
else if popularity...
else if collaborative...
```

This becomes difficult to extend.

With Strategy:

``` text
RecommendationService
          ↓
     Strategy
```

The algorithm can be replaced without changing the service.

### SOLID connection

This supports:

-   Open/Closed Principle
-   Dependency Inversion Principle

------------------------------------------------------------------------

# 17. Current Recommendation Implementations

## Genre-based

Uses listening history to identify genres the user listens to most.

Simplified flow:

``` text
User
 ↓
Listening History
 ↓
Songs
 ↓
Count genres
 ↓
Top genre
 ↓
Recommend songs
```

The generated project marks this strategy as the primary Spring bean.

------------------------------------------------------------------------

## Popularity-based

Placeholder strategy in the LLD.

Production implementation could use:

``` text
plays
likes
completion rate
recent popularity
unique listeners
```

A weighted popularity score could be computed.

------------------------------------------------------------------------

## Collaborative filtering

Also represented as a strategy placeholder.

Concept:

``` text
User A likes:
Song 1, Song 2, Song 3

User B likes:
Song 1, Song 2, Song 4

Therefore:
Recommend Song 4 to User A
```

Production implementations could use batch processing / ML rather than
calculating everything synchronously inside the request.

------------------------------------------------------------------------

# 18. Playlist Service

Operations:

``` text
createPlaylist
updatePlaylist
deletePlaylist
addSongs
removeSongs
```

Important validations:

-   Playlist exists
-   User owns playlist
-   Songs exist
-   Avoid duplicate song IDs where appropriate

### Concurrency problem

Two requests can arrive simultaneously:

``` text
Request A → add Song X
Request B → remove Song X
```

Without synchronization, the final state can be unpredictable.

------------------------------------------------------------------------

# 19. LockService

The original design used a boolean map.

That approach has a race condition:

``` text
if (!locks.containsKey(id)) {
    locks.put(id, true);
}
```

Two threads can both observe:

``` text
containsKey(id) == false
```

before either inserts.

The converted design uses:

``` text
ConcurrentHashMap<String, ReentrantLock>
```

with:

``` text
tryLock(timeout)
```

### Important interview point

`ConcurrentHashMap` alone does **not** make a multi-step business
operation atomic.

For example:

``` text
contains → modify → put
```

still needs synchronization if the whole sequence must be atomic.

------------------------------------------------------------------------

# 20. Distributed Lock --- Production Discussion

The in-memory `ReentrantLock` only protects threads inside one JVM.

With:

``` text
Instance A
Instance B
Instance C
```

each instance has its own lock.

Therefore:

``` text
Instance A lock != Instance B lock
```

For distributed coordination, consider:

-   Database optimistic locking
-   Database row locks
-   Redis-based distributed locks
-   Version numbers / compare-and-set
-   Partition ownership depending on architecture

A good interview answer:

> "The LLD uses ReentrantLock because we're inside one JVM. In
> production, if multiple service instances can modify the same
> playlist, I'd use optimistic concurrency or a distributed coordination
> mechanism rather than relying on a JVM-local lock."

------------------------------------------------------------------------

# 21. Downloads

Download flow:

``` text
User
 │
 ▼
DownloadController
 │
 ▼
DownloadService
 │
 ├── Validate user
 ├── Check PREMIUM
 ├── Check device limit
 ├── Check download limit
 └── Create download
```

Download states:

``` text
PENDING
IN_PROGRESS
COMPLETED
FAILED
```

The simplified implementation marks downloads as completed immediately.

Production:

``` text
PENDING
   ↓
IN_PROGRESS
   ↓
Download worker
   ↓
Object/CDN storage
   ↓
COMPLETED
```

This should normally be asynchronous.

------------------------------------------------------------------------

# 22. Download Constraints

The LLD demonstrates:

-   Premium-only downloads
-   Maximum number of devices
-   Maximum download count

These are examples of **business rules enforced in the service layer**.

Do not put important business rules only in the controller.

------------------------------------------------------------------------

# 23. Search

Search can target:

``` text
SONG
ARTIST
ALBUM
ALL
```

Example:

``` text
GET /api/search?q=love&type=SONG
```

Flow:

``` text
Controller
    ↓
SearchService
    ↓
Repository
    ↓
Search results
```

The service supports searching songs, artists and albums.

### Production improvements

-   Elasticsearch / OpenSearch
-   Prefix/inverted indexes
-   Ranking
-   Typo tolerance
-   Synonyms
-   Pagination
-   Search analytics

------------------------------------------------------------------------

# 24. REST API Cheat Sheet

Typical endpoints in the project:

## Playback

``` text
POST /api/playback/play
POST /api/playback/pause
POST /api/playback/resume
POST /api/playback/next
POST /api/playback/previous
GET  /api/playback/state
POST /api/playback/shuffle
POST /api/playback/repeat
POST /api/playback/position
```

## Playlist

``` text
POST   /api/playlists
PUT    /api/playlists/{id}
DELETE /api/playlists/{id}
POST   /api/playlists/{id}/songs
DELETE /api/playlists/{id}/songs
```

## Search

``` text
GET /api/search
```

## Streaming

``` text
GET /api/stream/{songId}
```

## Downloads

``` text
POST   /api/downloads
GET    /api/downloads
DELETE /api/downloads/{id}
```

## Recommendations

``` text
GET /api/recommendations
```

------------------------------------------------------------------------

# 25. Spring Boot Concepts Used

### `@SpringBootApplication`

Application entry point.

### `@RestController`

Exposes REST APIs.

### `@Service`

Business logic.

### `@Repository`

Persistence abstraction / repository layer.

### `@Component`

Used for simulator and other Spring-managed components.

### `@Primary`

Used for the default recommendation strategy when multiple
implementations exist.

### Constructor Injection

Preferred over field injection.

Why?

-   Dependencies are explicit
-   Easier unit testing
-   Supports immutability
-   Fails fast when required dependencies are missing

### Lombok

Used for reducing boilerplate:

``` text
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
```

------------------------------------------------------------------------

# 26. Thread Safety

The project uses:

``` text
ConcurrentHashMap
ReentrantLock
```

### ConcurrentHashMap

Useful for concurrent repository access.

But remember:

> Thread-safe collection ≠ thread-safe business transaction.

Example:

``` text
read
modify
write
```

may still need synchronization.

### ReentrantLock

Useful when:

-   Multiple operations form one critical section
-   Lock timeout is required
-   Explicit lock/unlock control is needed

Always release:

``` java
try {
    lock.lock();
    // critical section
} finally {
    lock.unlock();
}
```

------------------------------------------------------------------------

# 27. SOLID Principles in This LLD

## Single Responsibility

Separate:

``` text
PlaybackService
PlaylistService
DownloadService
SearchService
StreamingService
RecommendationService
```

Each has a focused responsibility.

## Open/Closed

Recommendation strategies can be added without changing the core
recommendation service.

## Liskov Substitution

Different `RecommendationStrategy` implementations can be substituted.

## Interface Segregation

Repository interfaces expose domain-specific operations rather than one
giant repository interface.

## Dependency Inversion

Services depend on abstractions:

``` text
Service → Repository interface
Service → RecommendationStrategy
```

instead of concrete persistence classes.

------------------------------------------------------------------------

# 28. Important Design Patterns

  Pattern                Where             Why
  ---------------------- ----------------- -------------------------------
  Repository             Repositories      Abstract persistence
  Strategy               Recommendations   Swap algorithms
  Service Layer          Services          Encapsulate business logic
  Dependency Injection   Spring            Loose coupling
  Cache                  Streaming         Reduce repeated storage reads
  Locking                Playlist          Protect concurrent updates

------------------------------------------------------------------------

# 29. Complete Playback Example

Suppose:

``` text
User = John
Playlist = My Rock
Queue = Song A → Song B → Song C
```

John presses Play.

``` text
POST /play
       ↓
PlaybackController
       ↓
PlaybackService
       ↓
Validate John
       ↓
Load playlist
       ↓
Build queue
       ↓
Create PlaybackSession
       ↓
currentSong = Song A
status = PLAYING
       ↓
Return stream URL
```

Client starts requesting:

``` text
bytes 0-1MB
bytes 1MB-2MB
bytes 2MB-3MB
```

Streaming service:

``` text
Request
  ↓
Cache
  ↓
HIT → return
MISS
  ↓
Storage
  ↓
Cache
  ↓
Return
```

While playing:

``` text
position = 180 / 200 sec
```

Since:

``` text
180 >= 90% × 200
```

the song is marked completed.

User presses Next:

``` text
Save history
    ↓
Get next queue item
    ↓
Song B
    ↓
Update session
    ↓
Continue streaming
```

------------------------------------------------------------------------

# 30. State Machine Thinking

Playback can be viewed as:

``` text
             ┌─────────┐
             │ STOPPED │
             └────┬────┘
                  │ play
                  ▼
             ┌─────────┐
       ┌────▶│ PLAYING │────┐
       │     └────┬────┘    │
       │          │ pause   │ stop
       │          ▼         ▼
       │     ┌─────────┐  STOPPED
       │     │ PAUSED  │
       │     └────┬────┘
       │          │ resume
       └──────────┘
```

Think about invalid transitions:

``` text
STOPPED → resume
PAUSED  → pause
```

The service should validate state transitions in a production-grade
implementation.

------------------------------------------------------------------------

# 31. Caching vs Persistence

Do not confuse these.

### Persistence

Source of truth:

``` text
Database / Object Storage
```

### Cache

Temporary copy:

``` text
Memory / Redis / CDN
```

If cache disappears:

``` text
System should continue working
```

but performance may degrade.

------------------------------------------------------------------------

# 32. Production Architecture

The interview LLD uses in-memory repositories and mock streaming.

A production architecture could look like:

``` text
                     Client
                       │
                       ▼
                 API Gateway
                       │
        ┌──────────────┼───────────────┐
        ▼              ▼               ▼
    Playback       Playlist         Search
    Service        Service          Service
        │              │               │
        ▼              ▼               ▼
     Database       Database       OpenSearch
        │
        ▼
    Event Bus
        │
        ├── Listening History
        ├── Recommendations
        └── Analytics

Client
  │
  ▼
CDN
  │
  ▼
Object Storage
  │
  └── Audio files
```

------------------------------------------------------------------------

# 33. Production Streaming Architecture

Instead of:

``` text
Client → Spring Boot → Audio bytes
```

prefer:

``` text
Client
  │
  ▼
API
  │
  ├── authenticate
  ├── authorize
  └── generate signed URL
           │
           ▼
          CDN
           │
           ▼
     Object Storage
```

The application should avoid becoming the bottleneck for large audio
payloads.

### Why?

-   Less JVM/network load
-   Better scalability
-   CDN edge caching
-   Lower latency
-   Better bandwidth utilization
-   Easier horizontal scaling

------------------------------------------------------------------------

# 34. Security / Authorization Discussion

The LLD has simplified authorization.

Production should verify:

### Streaming

``` text
Is user authenticated?
Does user have access to this song?
Is subscription tier sufficient?
Is the signed URL valid?
```

### Downloads

``` text
Premium?
Device authorized?
Download limit?
```

### Playlists

``` text
Does user own playlist?
Is playlist public/private?
```

Never trust:

``` text
userId
```

supplied blindly by the client.

In production, derive identity from the authenticated security
context/token.

------------------------------------------------------------------------

# 35. Signed URLs

For private audio:

``` text
Client
  ↓
API
  ↓
Authorize
  ↓
Generate short-lived signed URL
  ↓
Client downloads from CDN/object storage
```

The URL should expire.

Benefits:

-   Application doesn't proxy every byte
-   Access remains controlled
-   CDN can serve content directly

------------------------------------------------------------------------

# 36. Scaling Considerations

## Horizontal scaling

Run:

``` text
Playback Service × N
Streaming Service × N
Playlist Service × N
```

Avoid relying on JVM-local state.

### Bad for distributed production

``` text
ConcurrentHashMap
ReentrantLock
in-memory session
```

as the only source of state.

### Better

Externalize important state:

``` text
Database
Redis
Kafka / event bus
Object storage
CDN
```

------------------------------------------------------------------------

# 37. Events / Asynchronous Processing

Listening history and recommendations don't necessarily need to block
playback.

Example:

``` text
PlaybackService
      │
      ▼
Publish SongPlayed event
      │
      ▼
Message Queue
      │
      ├── History Service
      ├── Recommendation Service
      ├── Analytics
      └── User statistics
```

This reduces latency in the playback request.

------------------------------------------------------------------------

# 38. Reliability

Think about:

-   Retry
-   Idempotency
-   Timeouts
-   Circuit breakers
-   Dead-letter queues
-   Monitoring
-   Metrics
-   Distributed tracing

### Example

If download creation is retried:

``` text
Request
 ↓
Timeout
 ↓
Client retries
```

You should avoid accidentally creating duplicate downloads.

Use an idempotency key where appropriate.

------------------------------------------------------------------------

# 39. Database Modeling --- Production Discussion

Possible relational model:

``` text
users
artists
albums
songs
playlists
playlist_songs
playback_sessions
listening_history
downloads
```

Many-to-many playlist relationship:

``` text
Playlist
   │
   │ 1:N
   ▼
playlist_songs
   │
   │ N:1
   ▼
Song
```

Avoid storing huge playlist song arrays in a single relational row if
querying/managing individual songs is important.

------------------------------------------------------------------------

# 40. Potential Interview Follow-Up Questions

## Why not store audio in the database?

Large binary media is usually better suited to object storage.

``` text
DB → metadata
Object Storage → audio
CDN → delivery
```

------------------------------------------------------------------------

## Why Range Requests instead of HLS?

For this LLD:

> Simpler, standard HTTP, supports seeking and chunked progressive
> playback.

HLS becomes attractive when adaptive bitrate and live streaming are
important.

------------------------------------------------------------------------

## Why cache chunks instead of whole songs?

Because:

-   Songs can be large
-   Users may only listen to part of a song
-   Individual chunks are reusable
-   Memory usage is more controllable
-   CDN/cache can independently serve requested ranges

------------------------------------------------------------------------

## Why use Strategy Pattern?

Recommendation algorithms vary independently from recommendation
orchestration.

------------------------------------------------------------------------

## Why use Repository Pattern?

To decouple business logic from persistence.

------------------------------------------------------------------------

## Why ReentrantLock instead of synchronized?

`ReentrantLock` gives more explicit control, including timed acquisition
using `tryLock`.

`synchronized` is simpler and may be enough for many cases.

------------------------------------------------------------------------

## Why ConcurrentHashMap?

Multiple requests can access repositories concurrently.

But it does not automatically make a sequence of operations atomic.

------------------------------------------------------------------------

## What happens if cache goes down?

The system should fall back to persistent/object storage.

Cache is not the source of truth.

------------------------------------------------------------------------

## How would you support multiple devices?

Change:

``` text
one session per user
```

to:

``` text
one session per user + device
```

and store sessions in shared/distributed storage if horizontally scaled.

------------------------------------------------------------------------

## How would you support adaptive quality?

Use:

``` text
HLS / DASH
```

with multiple quality representations.

------------------------------------------------------------------------

## How would you prevent the streaming service from becoming a bottleneck?

Use:

``` text
CDN + object storage + signed URLs
```

rather than streaming all bytes through application servers.

------------------------------------------------------------------------

# 41. Current LLD vs Production

  Area              LLD                    Production
  ----------------- ---------------------- -------------------------------------
  Persistence       In-memory maps         DB
  Audio             Mock chunks            Object storage
  Delivery          HTTP range API         CDN + signed URLs
  Cache             JVM cache              Redis/CDN
  Sessions          One per user           User + device
  Lock              ReentrantLock          Optimistic/distributed coordination
  Search            Repository search      OpenSearch/Elasticsearch
  Recommendations   Simple strategies      Data/ML pipeline
  History           Synchronous            Event-driven possible
  Downloads         Immediate completion   Async worker
  Auth              Simplified             OAuth/JWT/security context
  Scaling           Single JVM             Horizontally scaled services

------------------------------------------------------------------------

# 42. Edge Cases to Mention

### Playback

-   Song does not exist
-   User does not exist
-   Empty playlist
-   End of queue
-   Previous on first song
-   Invalid seek position
-   Negative position
-   Position greater than duration
-   Invalid playback state

### Streaming

-   `start > end`
-   Negative byte range
-   Range beyond file size
-   Song does not exist
-   Cache miss
-   Cache full
-   Unauthorized user
-   Expired signed URL

### Playlist

-   Duplicate songs
-   Empty playlist
-   Playlist doesn't exist
-   User does not own playlist
-   Concurrent modifications

### Downloads

-   Free user
-   Device limit exceeded
-   Download limit exceeded
-   Duplicate download
-   Download failure
-   Device removed

------------------------------------------------------------------------

# 43. Complexity Cheat Sheet

Assuming hash-based repositories:

  Operation                               Approx. Complexity
  ---------------------------- -----------------------------
  Find entity by ID                                     O(1)
  Insert entity                                         O(1)
  Delete entity by ID                                   O(1)
  Search by scanning values                             O(N)
  Find song in playlist list                            O(P)
  Add song to playlist            O(P) if duplicate checking
  Shuffle queue                                         O(Q)
  Find next queue item              O(Q) depending on lookup
  Genre counting                 O(H) where H = history size

Production search should use an index/search engine rather than scanning
all records.

------------------------------------------------------------------------

# 44. 60-Second Interview Explanation

If asked to explain the design quickly:

> "I designed the system with separate domain, repository, service and
> controller layers. The core entity is PlaybackSession, which maintains
> the current song, position, queue, shuffle, repeat and playback state.
> PlaybackService builds queues from songs, albums or playlists and
> updates listening history as the user progresses.
>
> For streaming, I chose HTTP Range Requests because they're simple and
> standard. The client requests byte ranges, the server returns 206
> Partial Content with Content-Range, and chunks are cached using a
> song/start/end key. In production I would move audio delivery to
> object storage plus a CDN and use signed URLs.
>
> Recommendations use the Strategy Pattern so genre-based,
> popularity-based and collaborative filtering algorithms can be swapped
> independently. Repositories abstract persistence, and playlist updates
> use locking to protect concurrent modifications.
>
> The current implementation is intentionally in-memory and
> interview-sized, while production would use persistent storage,
> distributed caching, event-driven history/recommendations,
> multi-device sessions and distributed concurrency control."

------------------------------------------------------------------------

# 45. 30-Second Streaming Answer

> "I would use HTTP byte-range requests for this LLD. The client sends a
> Range header such as bytes 0 to 1 MB, and the server responds with 206
> Partial Content and Content-Range. Each chunk can be cached
> independently, and the client can prefetch upcoming chunks while
> playing the current one. For a production system requiring adaptive
> bitrate or live streaming, I'd use HLS or DASH and deliver media
> through CDN/object storage."

------------------------------------------------------------------------

# 46. Pattern Cheat Sheet

``` text
Repository
    → persistence abstraction

Strategy
    → interchangeable recommendation algorithms

Service Layer
    → business logic

Dependency Injection
    → loose coupling

Cache
    → faster repeated access

Lock
    → concurrency protection

State Machine
    → playback lifecycle
```

------------------------------------------------------------------------

# 47. Spring Boot Cheat Sheet

``` text
@SpringBootApplication
    → application entry point

@RestController
    → REST APIs

@Service
    → business logic

@Repository
    → repository layer

@Component
    → Spring-managed component

@Primary
    → default implementation

@RequiredArgsConstructor
    → constructor injection via Lombok

@Slf4j
    → structured application logging
```

------------------------------------------------------------------------

# 48. Key Things to Remember Before Interview

If you remember only these points:

1.  **PlaybackSession is the central state object.**
2.  **Repository Pattern abstracts persistence.**
3.  **Strategy Pattern makes recommendations extensible.**
4.  **HTTP Range Requests use `206 Partial Content`.**
5.  **Use `Content-Range` and `Accept-Ranges: bytes`.**
6.  **Cache key = `songId + start + end`.**
7.  **1 MB is the interview-level chunk size in this design.**
8.  **Cache is not the source of truth.**
9.  **ConcurrentHashMap does not make multi-step operations atomic.**
10. **ReentrantLock protects playlist modifications inside one JVM.**
11. **Distributed systems need shared state / optimistic locking /
    distributed coordination.**
12. **Production audio delivery should use CDN + object storage.**
13. **Signed URLs prevent exposing private audio indefinitely.**
14. **One session per user is simplified; production should support
    user + device.**
15. **History/recommendations can be event-driven.**
16. **Downloads should be asynchronous in production.**
17. **Search should use indexes/search engines at scale.**
18. **Always discuss authorization and edge cases.**

------------------------------------------------------------------------

# 49. Final Interview Checklist

Before finishing the interview, make sure you have covered:

``` text
☐ Requirements
☐ Core entities
☐ Relationships
☐ Service responsibilities
☐ Repository abstraction
☐ Playback state
☐ Queue management
☐ Shuffle/repeat
☐ Listening history
☐ Streaming protocol
☐ Range requests / 206
☐ Cache strategy
☐ Recommendation Strategy Pattern
☐ Concurrency
☐ Locking limitations
☐ Subscription rules
☐ Download lifecycle
☐ APIs
☐ Error/edge cases
☐ Scalability
☐ CDN/object storage
☐ Authentication/authorization
☐ Async events
```

------------------------------------------------------------------------

# 50. One-Line Mental Model

``` text
Music Platform
=
Playback State
+ Audio Delivery
+ Cache
+ Catalog
+ Playlists
+ History
+ Recommendations
+ Subscription Rules
+ Concurrency
```

**Interview priority:** explain the trade-offs, not just the classes.
