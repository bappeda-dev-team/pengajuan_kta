package cc.kertaskerja.pengajuan_kta.service.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthAttemptService {

    private final LoginAttemp loginAttemp = new LoginAttemp();
    private final SendOtpAttemp sendOtpAttemp = new SendOtpAttemp();

    public void loginSucceeded(String username) {
        loginAttemp.loginSucceeded(username);
    }

    public void loginFailed(String username) {
        loginAttemp.loginFailed(username);
    }

    public boolean loginBlocked(String username) {
        return loginAttemp.loginBlocked(username);
    }

    public void sendOtpSucceeded(String username) {
        sendOtpAttemp.sendOtpSucceeded(username);
    }

    public void sendOtpFailed(String username) {
        sendOtpAttemp.sendOtpFailed(username);
    }

    public boolean sendOtpBlocked (String msisdn) {
        return sendOtpAttemp.sentOtpBlocked(msisdn);
    }

    @Getter
    private static class LoginAttemp {
        private static final int MAX_ATTEMPTS = 3;
        private static final long LOCK_TIME_MS = 60 * 1000;
        private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

        public void loginSucceeded(String username) {
            attemptsCache.remove(username);
        }

        public void loginFailed(String username) {
            AttemptInfo info = attemptsCache.getOrDefault(username, new AttemptInfo(0, null));
            int newAttempts = info.getAttempts() + 1;
            Instant lockTime = (newAttempts >= MAX_ATTEMPTS) ? Instant.now() : info.getLockedUntil();
            attemptsCache.put(username, new AttemptInfo(newAttempts, lockTime));
        }

        public boolean loginBlocked(String username) {
            return getRemainingLockTime(username) > 0;
        }

        public long getRemainingLockTime(String username) {
            AttemptInfo info = attemptsCache.get(username);
            if (info == null || info.getLockedUntil() == null) return 0;

            long elapsed = Duration.between(info.getLockedUntil(), Instant.now()).toMillis();
            long remaining = LOCK_TIME_MS - elapsed;

            if (remaining <= 0) {
                attemptsCache.remove(username);
                return 0;
            }
            return remaining;
        }

        public Instant getUnlockTime(String username) {
            AttemptInfo info = attemptsCache.get(username);
            if (info == null || info.getLockedUntil() == null) return null;
            return info.getLockedUntil().plusMillis(LOCK_TIME_MS);
        }
    }

    @Getter
    private static class SendOtpAttemp {
        private static final int MAX_ATTEMPTS = 1;
        private static final long LOCK_TIME_MS = 60 * 1000; // 1 minute
        private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

        public void sendOtpFailed(String username) {
            attemptsCache.remove(username);
        }

        public void sendOtpSucceeded(String msisdn) {
            AttemptInfo info = attemptsCache.getOrDefault(msisdn, new AttemptInfo(0, null));
            int newAttempts = info.getAttempts() + 1;
            Instant lockTime = (newAttempts >= MAX_ATTEMPTS) ? Instant.now() : info.getLockedUntil();
            attemptsCache.put(msisdn, new AttemptInfo(newAttempts, lockTime));
        }

        public boolean sentOtpBlocked(String msisdn) {
            return getRemainingLockTime(msisdn) > 0;
        }

        public long getRemainingLockTime(String msisdn) {
            AttemptInfo info = attemptsCache.get(msisdn);
            if (info == null || info.getLockedUntil() == null) return 0;

            long elapsed = Duration.between(info.getLockedUntil(), Instant.now()).toMillis();
            long remaining = LOCK_TIME_MS - elapsed;

            if (remaining <= 0) {
                attemptsCache.remove(msisdn);
                return 0;
            }
            return remaining;
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class AttemptInfo {
        private final int attempts;
        private final Instant lockedUntil;
    }
}
