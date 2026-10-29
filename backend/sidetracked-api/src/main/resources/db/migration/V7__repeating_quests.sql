-- ============================================================
-- V7: REPEATING QUESTS
-- ============================================================
--
-- A quest can repeat on a schedule, e.g. medication every 8 hours
-- that's fine anywhere from 6 to 10 hours after the last dose:
--
--   repeat_every_hours  8   the ideal gap between completions
--   window_start_hours  6   earliest on-time completion
--   window_end_hours    10  after this it's overdue
--
-- All three are NULL for a regular one-off quest. Each completion
-- is recorded in quest_completions (V4).

ALTER TABLE quest
    ADD COLUMN repeat_every_hours INT NULL,
    ADD COLUMN window_start_hours INT NULL,
    ADD COLUMN window_end_hours   INT NULL;
