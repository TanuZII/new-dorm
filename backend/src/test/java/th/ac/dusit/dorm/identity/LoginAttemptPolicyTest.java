package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptPolicyTest {

    @Test
    void locksAccountForFifteenMinutesAfterFifthFailure() {
        var now = Instant.parse("2026-07-29T08:00:00Z");
        var policy = new LoginAttemptPolicy(5, 15, Clock.fixed(now, ZoneOffset.UTC));
        var attempts = new LoginAttemptState();

        for (int i = 0; i < 5; i++) {
            policy.recordFailure(attempts);
        }

        assertThat(attempts.failedAttempts()).isEqualTo(5);
        assertThat(attempts.lockedUntil()).isEqualTo(Instant.parse("2026-07-29T08:15:00Z"));
        assertThat(policy.isLocked(attempts)).isTrue();
    }

    @Test
    void successfulLoginClearsPreviousFailures() {
        var policy = new LoginAttemptPolicy(5, 15, Clock.systemUTC());
        var attempts = new LoginAttemptState();
        policy.recordFailure(attempts);
        policy.recordSuccess(attempts);

        assertThat(attempts.failedAttempts()).isZero();
        assertThat(attempts.lockedUntil()).isNull();
    }
}
