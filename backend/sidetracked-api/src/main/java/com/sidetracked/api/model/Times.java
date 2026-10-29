package com.sidetracked.api.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/*
 * All timestamps are stored in UTC, so "8 hours after the last
 * dose" means 8 real hours even across daylight-saving changes or
 * on a server in another time zone.
 *
 * The database columns hold UTC clock time (DATETIME). Times sent
 * to the browser are Instants (e.g. 2026-10-29T22:49:22Z), which
 * every device shows in its own local time.
 */
public final class Times {

    private Times() {
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Instant toInstant(LocalDateTime utc) {
        return utc == null ? null : utc.toInstant(ZoneOffset.UTC);
    }
}
