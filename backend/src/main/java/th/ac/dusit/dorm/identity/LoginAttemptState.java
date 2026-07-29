package th.ac.dusit.dorm.identity;

import java.time.Instant;

public final class LoginAttemptState {
    private int failedAttempts;
    private Instant lockedUntil;

    public int failedAttempts() {
        return failedAttempts;
    }

    public Instant lockedUntil() {
        return lockedUntil;
    }

    void fail() {
        failedAttempts++;
    }

    void lockUntil(Instant until) {
        lockedUntil = until;
    }

    void reset() {
        failedAttempts = 0;
        lockedUntil = null;
    }
}

