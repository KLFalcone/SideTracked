-- ============================================================
-- V4: QUEST COMPLETIONS + XP HISTORY
-- ============================================================
--
-- quest_completions  one row EACH time a quest is completed,
--                    with a timestamp. Today a quest completes
--                    once; repeating quests (e.g. medication
--                    every 8 hours) will add more rows.
--
-- xp_events          one row per XP change, like a bank
--                    statement. The unique key allows exactly
--                    one event per thing that earned (or
--                    removed) XP, so nothing is paid twice.
--
-- players.xp stays as the running total and always equals the
-- sum of the user's xp_events.
--
-- Timestamps that were never recorded before this migration are
-- set to the migration time and flagged as backfilled.


-- ------------------------------------------------------------
-- PLAYER COUNTER RENAME
-- ------------------------------------------------------------

-- "encounters" only ever counted completed Rabbit Holes.
ALTER TABLE players
    RENAME COLUMN encounters_completed TO rabbit_holes_completed;


-- ------------------------------------------------------------
-- NEW TABLES
-- ------------------------------------------------------------

CREATE TABLE quest_completions (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    quest_id     BIGINT      NOT NULL,
    completed_at DATETIME(6) NOT NULL,
    backfilled   BIT(1)      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_quest_completions_quest
        FOREIGN KEY (quest_id) REFERENCES quest (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- source_type + source_id point at what caused the change:
--   QUEST_COMPLETED        quest_completions.id
--   QUEST_DELETED          quest_completions.id being reversed
--   RABBIT_HOLE_COMPLETED  user_rabbit_holes.id
--   LEGACY                 0 (XP from before this history existed)
--
-- label keeps a readable description (e.g. the quest title) so the
-- history still makes sense after a quest is deleted.
CREATE TABLE xp_events (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    source_type ENUM ('LEGACY', 'QUEST_COMPLETED', 'QUEST_DELETED',
                      'RABBIT_HOLE_COMPLETED') NOT NULL,
    source_id   BIGINT       NOT NULL,
    amount      INT          NOT NULL,
    label       VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    backfilled  BIT(1)       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_xp_events_source
        UNIQUE (user_id, source_type, source_id),
    CONSTRAINT fk_xp_events_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- ------------------------------------------------------------
-- BACKFILL EXISTING HISTORY
-- ------------------------------------------------------------

-- Every already-completed quest gets one completion.
INSERT INTO quest_completions (quest_id, completed_at, backfilled)
SELECT q.id, NOW(6), b'1'
FROM quest q
WHERE q.completed = b'1';

-- XP for those completions.
INSERT INTO xp_events (user_id, source_type, source_id, amount,
                       label, created_at, backfilled)
SELECT q.user_id,
       'QUEST_COMPLETED',
       qc.id,
       q.xp,
       LEFT(COALESCE(TRIM(q.title), 'Quest'), 255),
       qc.completed_at,
       b'1'
FROM quest_completions qc
JOIN quest q ON q.id = qc.quest_id;

-- XP for completed Rabbit Holes. Those completed after V3 already
-- have a real completion time.
INSERT INTO xp_events (user_id, source_type, source_id, amount,
                       label, created_at, backfilled)
SELECT urh.user_id,
       'RABBIT_HOLE_COMPLETED',
       urh.id,
       t.xp,
       t.title,
       COALESCE(urh.completed_at, NOW(6)),
       urh.completed_at IS NULL
FROM user_rabbit_holes urh
JOIN topics t ON t.id = urh.topic_id
WHERE urh.status = 'COMPLETED';

-- Whatever XP the player has beyond those events came from earlier
-- versions of the app. One LEGACY event makes the totals match.
INSERT INTO xp_events (user_id, source_type, source_id, amount,
                       label, created_at, backfilled)
SELECT p.user_id,
       'LEGACY',
       0,
       p.xp - COALESCE(e.total, 0),
       'XP from earlier versions of SideTracked',
       NOW(6),
       b'1'
FROM players p
LEFT JOIN (
    SELECT user_id, SUM(amount) AS total
    FROM xp_events
    GROUP BY user_id
) e ON e.user_id = p.user_id
WHERE p.xp <> COALESCE(e.total, 0);
