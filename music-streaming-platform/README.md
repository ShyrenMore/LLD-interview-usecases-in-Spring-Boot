# Music Streaming LLD — Spring Boot

Converted from the supplied Java LLD into a Spring Boot project using `com.personal.lld`.

## Structure

```text
com.personal.lld
├── domain
├── repository
│   └── impl
├── service
│   └── strategy
├── controller
└── simulator
```

## Key design ideas

- **Repository Pattern:** persistence is abstracted behind repository interfaces; the project uses thread-safe in-memory implementations.
- **Strategy Pattern:** recommendation algorithms are represented by `RecommendationStrategy`; genre-based, popularity-based, and collaborative-filtering strategies are available.
- **Service Layer:** playback, playlists, downloads, streaming, search, recommendations, caching, and locking have separate responsibilities.
- **Playback Session:** one session per user in the supplied design; a production design would normally support multiple devices/sessions.
- **Cache:** streaming chunks are cached with an LRU-style eviction approach.
- **Concurrency:** playlist updates use a lock. The original boolean map lock was strengthened to `ReentrantLock.tryLock()` so concurrent requests do not race through `containsKey()` + `put()`.
- **Structured logging:** the simulator uses SLF4J/Lombok `@Slf4j` instead of `System.out`.

## Main flow

```text
Client
  ↓
Controller
  ↓
Service
  ├── Repository
  ├── Strategy
  ├── Cache
  └── other services
```

## Playback flow

```text
play(user, source, sourceId)
        ↓
validate user
        ↓
build queue
        ↓
create/update PlaybackSession
        ↓
StreamingService.getStreamUrl()
        ↓
PlaybackStateResponse
```

Supported sources:

```text
SONG
ALBUM
PLAYLIST
```

Playback supports:

```text
PLAYING
PAUSED
STOPPED
```

and repeat modes:

```text
OFF
ONE
```

## Important production discussion points

1. Replace in-memory repositories with durable storage.
2. Use Redis/distributed locking or atomic database updates when multiple application instances modify the same playlist/session.
3. Use a real object store/CDN for audio and signed URLs for protected streaming.
4. Use Redis/object caching for hot audio chunks.
5. Use an event stream such as Kafka/Kinesis for listening events and recommendation pipelines.
6. Make downloads asynchronous rather than marking them complete immediately.
7. Add authorization checks to streaming/download/delete operations.
8. Persist playback sessions if cross-device resume is required.
9. Use a proper recommendation pipeline for popularity and collaborative filtering.
10. Use timezone-aware date handling for daily listening history instead of simple epoch-day arithmetic.

## Run

```bash
mvn spring-boot:run
```

The `MusicStreamingSimulation` runs automatically on startup and demonstrates:

1. User creation
2. Search
3. Playback
4. Playlist creation
5. Premium download
6. Playback position update
7. Recommendations

## Interview focus

Be prepared to explain:

- Why Strategy Pattern is useful for recommendations.
- Why Repository Pattern is useful.
- Why `ReentrantLock.tryLock()` is safer than a `ConcurrentHashMap<Boolean>` check-and-put sequence.
- How audio chunk caching works.
- How you would implement HTTP Range Requests in a real streaming API.
- Why a CDN/object store is preferable to serving audio bytes from the application.
- How to make download processing asynchronous.
- How listening history becomes an event stream for recommendations.
- How to support multiple devices and concurrent playback sessions.
- How to make authorization and signed streaming URLs secure.

## Streaming Protocols

Audio streaming needs to deliver data progressively so playback can begin before the complete audio file has been downloaded. Three common approaches are HTTP Range Requests, HLS, and DASH.

### 1. HTTP Range Requests (Selected for this LLD)

HTTP Range Requests allow a client to request a specific byte range of an audio file. The server responds with `206 Partial Content`, allowing the client to buffer and play chunks progressively.

Example:

```text
Client → GET /api/stream/song123
         Range: bytes=0-1048575

Server → 206 Partial Content
         Accept-Ranges: bytes
         Content-Range: bytes 0-1048575/5242880
         Body: first 1 MB

Client → Range: bytes=1048576-2097151
         ...
```

The client can request the next chunk while the current chunk is playing, and can also request a different byte range when the user seeks within the track.

**Why this approach is used here:**

- Simple and interview-friendly.
- Uses standard HTTP functionality.
- Works with normal HTTP clients, web servers, and CDNs.
- Supports seeking efficiently.
- Fits the chunk-based streaming and cache design in this LLD.

**Limitations:**

- Client/application must manage buffering and chunk requests.
- Does not provide automatic adaptive quality switching.
- The source audio is generally represented as a single file.

### 2. HLS (HTTP Live Streaming)

HLS breaks audio into small segments and exposes an `.m3u8` playlist describing those segments. The client downloads the segments progressively.

Conceptually:

```text
/api/stream/song123.m3u8
        ↓
segment1.ts → segment2.ts → segment3.ts → ...
```

**Advantages:** adaptive quality, standardized delivery, error recovery, and support for both live and on-demand streaming.

**Trade-off:** more infrastructure and complexity because media must be segmented and playlists maintained.

### 3. DASH (Dynamic Adaptive Streaming over HTTP)

DASH is another adaptive streaming approach. It uses a manifest to describe available media representations, allowing clients to select among different quality levels and tracks.

**Advantages:** flexible, feature-rich, and well suited to adaptive bitrate streaming.

**Trade-off:** more complex infrastructure than simple byte-range streaming.

### Protocol Comparison

| Approach | Main mechanism | Adaptive quality | Seeking | Complexity | Interview choice |
|---|---|---|---|---|---|
| HTTP Range | Byte ranges | No | Yes | Low | **Yes** |
| HLS | Segmented media + `.m3u8` | Yes | Yes | Medium | No |
| DASH | Segments + manifest | Yes | Yes | High | No |

### Implementation Details for This LLD

The intended interview-level implementation uses:

- **Chunk size:** 1 MB (`1048576` bytes), configurable.
- **Cache key:** `chunk_{songId}_{start}_{end}`.
- **Response:** `206 Partial Content` for a valid range request.
- **Important headers:** `Accept-Ranges: bytes` and `Content-Range`.
- **Client behavior:** request chunks sequentially or in parallel to maintain a playback buffer.
- **Prefetching:** request the next chunk while the current chunk is playing.

The current project contains a mock `StreamingService` and chunk cache to demonstrate the design. The controller-level implementation is intentionally simplified; a production implementation should return the correct HTTP status, headers, content type, and byte range and should enforce authorization before serving protected audio.

### Real-World Architecture

For a production system, the application should generally not stream large audio files directly from the application server:

```text
Client
  ↓
API / Authorization
  ↓
Signed URL
  ↓
CDN
  ↓
Object Storage
  ↓
Audio File
```

The application handles authentication/authorization and issues a short-lived signed URL. The CDN/object store handles high-volume byte-range delivery, reducing load on application servers.
