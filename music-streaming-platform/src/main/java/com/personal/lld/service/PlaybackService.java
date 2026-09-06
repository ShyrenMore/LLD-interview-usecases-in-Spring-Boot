package com.personal.lld.service;

import com.personal.lld.domain.*;
import com.personal.lld.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaybackService {
    private static final double COMPLETION_THRESHOLD = .9;
    private final PlaybackSessionRepository sessionRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final ListeningHistoryRepository historyRepository;
    private final StreamingService streamingService;

    public PlaybackStateResponse play(String uid, PlaybackSource source, String sourceId, Long start) {
        userRepository.findById(uid).orElseThrow(() -> new IllegalArgumentException("User not found: " + uid));
        List<String> q = buildQueue(source, sourceId);
        if (q.isEmpty()) throw new IllegalArgumentException("No songs found for source: " + sourceId);
        long pos = start == null ? 0 : start;
        String current = q.get(0);
        PlaybackSession s = sessionRepository.findByUserId(uid).orElseGet(() -> new PlaybackSession());
        if (s.getSessionId() == null) {
            String id = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            s = new PlaybackSession(id, "SESS_" + id.substring(0, 8), uid, current, pos, source, sourceId, q, false, RepeatMode.OFF, PlaybackStatus.PLAYING, "device_" + uid, now, now);
        } else {
            s.setCurrentSongId(current);
            s.setCurrentPosition(pos);
            s.setQueue(q);
            s.setPlaybackSource(source);
            s.setSourceId(sourceId);
            s.setStatus(PlaybackStatus.PLAYING);
            s.setLastUpdatedAt(System.currentTimeMillis());
        }
        s = sessionRepository.save(s);
        return response(s, streamingService.getStreamUrl(current, uid));
    }

    public PlaybackStateResponse pause(String sid) {
        PlaybackSession s = get(sid);
        s.setStatus(PlaybackStatus.PAUSED);
        touch(s);
        return response(sessionRepository.save(s), null);
    }

    public PlaybackStateResponse resume(String sid) {
        PlaybackSession s = get(sid);
        s.setStatus(PlaybackStatus.PLAYING);
        touch(s);
        return response(sessionRepository.save(s), null);
    }

    public PlaybackStateResponse skipNext(String sid) {
        PlaybackSession s = get(sid);
        saveHistory(s.getUserId(), s.getCurrentSongId(), s.getCurrentPosition(), false);
        String next = next(s);
        if (next == null) s.setStatus(PlaybackStatus.STOPPED);
        else {
            s.setCurrentSongId(next);
            s.setCurrentPosition(0);
        }
        touch(s);
        s = sessionRepository.save(s);
        return response(s, next == null ? null : streamingService.getStreamUrl(next, s.getUserId()));
    }

    public PlaybackStateResponse skipPrevious(String sid) {
        PlaybackSession s = get(sid);
        String prev = previous(s);
        if (prev != null) {
            s.setCurrentSongId(prev);
            s.setCurrentPosition(0);
        }
        touch(s);
        s = sessionRepository.save(s);
        return response(s, prev == null ? null : streamingService.getStreamUrl(prev, s.getUserId()));
    }

    public PlaybackStateResponse getState(String sid) {
        return response(get(sid), null);
    }

    public void updatePosition(String sid, long position) {
        if (position < 0) throw new IllegalArgumentException("Position cannot be negative");
        PlaybackSession s = get(sid);
        s.setCurrentPosition(position);
        touch(s);
        sessionRepository.save(s);
        songRepository.findBySongId(s.getCurrentSongId()).ifPresent(song -> saveHistory(s.getUserId(), s.getCurrentSongId(), position, position >= song.getDuration() * COMPLETION_THRESHOLD));
    }

    public PlaybackStateResponse toggleShuffle(String sid, boolean enabled) {
        PlaybackSession s = get(sid);
        s.setShuffleMode(enabled);
        if (enabled) {
            List<String> q = new ArrayList<>(s.getQueue());
            if (q.size() > 1) {
                String current = s.getCurrentSongId();
                Collections.shuffle(q);
                q.remove(current);
                q.add(0, current);
                s.setQueue(q);
            }
        }
        touch(s);
        return response(sessionRepository.save(s), null);
    }

    public PlaybackStateResponse setRepeatMode(String sid, RepeatMode mode) {
        PlaybackSession s = get(sid);
        s.setRepeatMode(Objects.requireNonNull(mode));
        touch(s);
        return response(sessionRepository.save(s), null);
    }

    private List<String> buildQueue(PlaybackSource st, String id) {
        return switch (st) {
            case SONG -> Collections.singletonList(id);
            case ALBUM ->
                    albumRepository.findByAlbumId(id).map(a -> songRepository.findByAlbumId(id).stream().map(Song::getSongId).collect(Collectors.toList())).orElse(Collections.emptyList());
            case PLAYLIST ->
                    playlistRepository.findByPlaylistId(id).map(p -> new ArrayList<>(p.getSongIds())).orElse(new ArrayList<>());
        };
    }

    private String next(PlaybackSession s) {
        List<String> q = s.getQueue();
        if (q.isEmpty()) return null;
        int i = q.indexOf(s.getCurrentSongId());
        if (s.getRepeatMode() == RepeatMode.ONE) return s.getCurrentSongId();
        return i >= 0 && i + 1 < q.size() ? q.get(i + 1) : null;
    }

    private String previous(PlaybackSession s) {
        int i = s.getQueue().indexOf(s.getCurrentSongId());
        return i > 0 ? s.getQueue().get(i - 1) : null;
    }

    private void saveHistory(String u, String song, long duration, boolean completed) {
        long now = System.currentTimeMillis();
        ListeningHistory h = historyRepository.findByUserIdAndSongIdAndDate(u, song, now);
        if (h == null) {
            h = new ListeningHistory(UUID.randomUUID().toString(), u, song, now, duration, completed);
        } else {
            h.setPlayDuration(duration);
            h.setCompleted(completed);
            h.setPlayedAt(now);
        }
        historyRepository.save(h);
    }

    private PlaybackSession get(String sid) {
        return sessionRepository.findBySessionId(sid).orElseThrow(() -> new IllegalArgumentException("Session not found: " + sid));
    }

    private void touch(PlaybackSession s) {
        s.setLastUpdatedAt(System.currentTimeMillis());
    }

    private PlaybackStateResponse response(PlaybackSession s, String url) {
        PlaybackStateResponse r = new PlaybackStateResponse();
        r.setSessionId(s.getSessionId());
        r.setCurrentSong(songRepository.findBySongId(s.getCurrentSongId()).orElse(null));
        r.setCurrentPosition(s.getCurrentPosition());
        r.setQueue(s.getQueue().stream().map(id -> songRepository.findBySongId(id).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList()));
        r.setShuffleMode(s.isShuffleMode());
        r.setRepeatMode(s.getRepeatMode());
        r.setStatus(s.getStatus());
        r.setStreamUrl(url);
        return r;
    }

    @Data
    public static class PlaybackStateResponse {
        private String sessionId;
        private Song currentSong;
        private long currentPosition;
        private List<Song> queue;
        private boolean shuffleMode;
        private RepeatMode repeatMode;
        private PlaybackStatus status;
        private String streamUrl;
    }
}
