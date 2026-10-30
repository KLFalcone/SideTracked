package com.sidetracked.api.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/*
 * ============================================================
 * SIGN-IN RATE LIMITS
 * ============================================================
 *
 * Slows down password guessing and account spam.
 *
 * - Wrong passwords: after 5 failed sign-ins for one email within
 *   15 minutes, that email is blocked until the oldest failure
 *   is 15 minutes old. Counting per email (not per address) works
 *   even when every request arrives through the same proxy.
 * - New accounts: at most 20 per hour across the whole app.
 *
 * Kept in memory, so a restart clears it. That's fine for one
 * small server.
 */
@Component
public class SignInRateLimiter {

    static final int MAX_FAILED_SIGN_INS = 5;
    static final Duration FAILED_SIGN_IN_WINDOW = Duration.ofMinutes(15);

    static final int MAX_REGISTRATIONS = 20;
    static final Duration REGISTRATION_WINDOW = Duration.ofHours(1);

    private final Clock clock;

    private final Map<String, Deque<Instant>> failedSignIns = new ConcurrentHashMap<>();
    private final Deque<Instant> registrations = new ArrayDeque<>();

    public SignInRateLimiter() {
        this(Clock.systemUTC());
    }

    SignInRateLimiter(Clock clock) {
        this.clock = clock;
    }


    public void checkSignIn(String email) {

        Deque<Instant> failures = failedSignIns.get(key(email));

        if (failures == null) {
            return;
        }

        synchronized (failures) {

            dropOlderThan(failures, FAILED_SIGN_IN_WINDOW);

            if (failures.size() >= MAX_FAILED_SIGN_INS) {
                throw tooMany("Too many sign-in attempts. Try again in 15 minutes.");
            }
        }
    }

    public void recordFailedSignIn(String email) {

        Deque<Instant> failures = failedSignIns.computeIfAbsent(
                key(email),
                ignored -> new ArrayDeque<>()
        );

        synchronized (failures) {
            dropOlderThan(failures, FAILED_SIGN_IN_WINDOW);
            failures.addLast(clock.instant());
        }
    }

    public void recordSuccessfulSignIn(String email) {
        failedSignIns.remove(key(email));
    }

    /*
     * Counts the attempt, or refuses it when the hourly limit
     * is used up.
     */
    public synchronized void checkAndRecordRegistration() {

        dropOlderThan(registrations, REGISTRATION_WINDOW);

        if (registrations.size() >= MAX_REGISTRATIONS) {
            throw tooMany("Too many new accounts right now. Try again later.");
        }

        registrations.addLast(clock.instant());
    }


    private void dropOlderThan(Deque<Instant> times, Duration window) {

        Instant cutoff = clock.instant().minus(window);

        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.removeFirst();
        }
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseStatusException tooMany(String message) {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
