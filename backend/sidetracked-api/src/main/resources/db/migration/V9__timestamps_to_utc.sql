-- ============================================================
-- V9: STORE TIMESTAMPS IN UTC
-- ============================================================
--
-- Until now timestamps were saved as the local clock time of the
-- machine running SideTracked (US Eastern). From now on the app
-- saves UTC, so elapsed time is always real time: "8 hours after
-- the last dose" stays correct across daylight-saving changes and
-- on a server in another time zone.
--
-- Every existing row was created on 2026-10-28/29, during US
-- Eastern Daylight Time (UTC-4), so each stored time moves forward
-- exactly 4 hours. A brand-new database has no rows, so this
-- changes nothing there.
--
-- NOTE: this assumes it runs before daylight saving ends
-- (2026-11-01 02:00 local). Rows created after that would be
-- UTC-5.

UPDATE quest_completions
SET completed_at = DATE_ADD(completed_at, INTERVAL 4 HOUR);

UPDATE xp_events
SET created_at = DATE_ADD(created_at, INTERVAL 4 HOUR);

UPDATE user_rabbit_holes
SET discovered_at = DATE_ADD(discovered_at, INTERVAL 4 HOUR),
    completed_at  = DATE_ADD(completed_at, INTERVAL 4 HOUR);

UPDATE topics
SET created_at = DATE_ADD(created_at, INTERVAL 4 HOUR);

UPDATE users
SET created_at = DATE_ADD(created_at, INTERVAL 4 HOUR);
