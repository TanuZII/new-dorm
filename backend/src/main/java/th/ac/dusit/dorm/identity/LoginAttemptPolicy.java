package th.ac.dusit.dorm.identity;

import java.time.Clock;
import java.time.Duration;

public final class LoginAttemptPolicy {
    private final int maximumFailures;
    private final Duration lockDuration;
    private final Clock clock;

    public LoginAttemptPolicy(int maximumFailures, int lockMinutes, Clock clock) {
        this.maximumFailures = maximumFailures;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
        this.clock = clock;
    }

    public void recordFailure(LoginAttemptState state) {
        state.fail();
        if (state.failedAttempts() >= maximumFailures) {
            state.lockUntil(clock.instant().plus(lockDuration));
        }
    }

    public void recordSuccess(LoginAttemptState state) {
        state.reset();
    }

    public boolean isLocked(LoginAttemptState state) {
        return state.lockedUntil() != null && state.lockedUntil().isAfter(clock.instant());
    }
}
