package com.sidetracked.api.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class SignInRateLimiterTest {

    private MutableClock clock;
    private SignInRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-30T12:00:00Z"));
        limiter = new SignInRateLimiter(clock);
    }

    @Test
    void blocksAnEmailAfterFiveFailedSignIns() {

        failSignIns("kat@example.com", 5);

        assertThatThrownBy(() -> limiter.checkSignIn("kat@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Too many sign-in attempts");
    }

    @Test
    void sameEmailWithDifferentCaseCountsTogether() {

        failSignIns("Kat@Example.com ", 5);

        assertThatThrownBy(() -> limiter.checkSignIn("kat@example.com"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void otherEmailsAreNotBlocked() {

        failSignIns("kat@example.com", 5);

        assertThatCode(() -> limiter.checkSignIn("someone@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void unblocksAfterFifteenMinutes() {

        failSignIns("kat@example.com", 5);

        clock.advance(SignInRateLimiter.FAILED_SIGN_IN_WINDOW);

        assertThatCode(() -> limiter.checkSignIn("kat@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void successfulSignInClearsEarlierFailures() {

        failSignIns("kat@example.com", 4);
        limiter.recordSuccessfulSignIn("kat@example.com");
        failSignIns("kat@example.com", 4);

        assertThatCode(() -> limiter.checkSignIn("kat@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void limitsNewAccountsPerHour() {

        for (int i = 0; i < SignInRateLimiter.MAX_REGISTRATIONS; i++) {
            limiter.checkAndRecordRegistration();
        }

        assertThatThrownBy(() -> limiter.checkAndRecordRegistration())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Too many new accounts");

        clock.advance(SignInRateLimiter.REGISTRATION_WINDOW);

        assertThatCode(() -> limiter.checkAndRecordRegistration())
                .doesNotThrowAnyException();
    }


    private void failSignIns(String email, int times) {

        for (int i = 0; i < times; i++) {
            limiter.checkSignIn(email);
            limiter.recordFailedSignIn(email);
        }
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
