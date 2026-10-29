-- ============================================================
-- V8: XP HISTORY CAN RECORD AN UNDONE COMPLETION
-- ============================================================
--
-- Undoing the last completion of a repeating quest takes back the
-- XP it earned, recorded as COMPLETION_UNDONE. Adds the new value
-- to the allowed list; existing rows are unchanged.

ALTER TABLE xp_events
    MODIFY source_type ENUM ('COMPLETION_UNDONE', 'LEGACY',
                             'QUEST_COMPLETED', 'QUEST_DELETED',
                             'RABBIT_HOLE_COMPLETED') NOT NULL;
