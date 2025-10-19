package cc.kertaskerja.pengajuan_kta.service.auth;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.time.Duration;
import java.time.Instant;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_MS = 60 * 1000;
    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
    }

    public void loginFailed(String username) {
        AttemptInfo info = attemptsCache.getOrDefault(username, new AttemptInfo(0, null));
        int newAttempts = info.attempts + 1;
        Instant lockTime = (newAttempts >= MAX_ATTEMPTS) ? Instant.now() : info.lockedUntil;
        attemptsCache.put(username, new AttemptInfo(newAttempts, lockTime));
    }

    public boolean isBlocked(String username) {
        return getRemainingLockTime(username) > 0;
    }

    public long getRemainingLockTime(String username) {
        AttemptInfo info = attemptsCache.get(username);
        if (info == null || info.lockedUntil == null) return 0;

        long elapsed = Duration.between(info.lockedUntil, Instant.now()).toMillis();
        long remaining = LOCK_TIME_MS - elapsed;
        if (remaining <= 0) {
            attemptsCache.remove(username);
            return 0;
        }
        return remaining;
    }

    public Instant getUnlockTime(String username) {
        AttemptInfo info = attemptsCache.get(username);
        if (info == null || info.lockedUntil == null) return null;
        return info.lockedUntil.plusMillis(LOCK_TIME_MS);
    }

    private record AttemptInfo(int attempts, Instant lockedUntil) {}
}

