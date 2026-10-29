-- ============================================================
-- V2: USERS AND OWNERSHIP
-- ============================================================
--
-- Every player profile and quest now belongs to a user.
--
-- Data created before users existed is adopted by one local
-- user. That user is only created when such data exists, so a
-- brand-new database (e.g. production) starts with no users,
-- no players, and no quests.

CREATE TABLE users (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(100) NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO users (display_name, created_at)
SELECT 'Local Developer', NOW(6)
FROM DUAL
WHERE EXISTS (SELECT 1 FROM players)
   OR EXISTS (SELECT 1 FROM quest);


-- Players: exactly one game profile per user.

ALTER TABLE players
    ADD COLUMN user_id BIGINT NULL;

UPDATE players
SET user_id = (SELECT MIN(id) FROM users);

ALTER TABLE players
    MODIFY user_id BIGINT NOT NULL,
    ADD CONSTRAINT uk_players_user UNIQUE (user_id),
    ADD CONSTRAINT fk_players_user
        FOREIGN KEY (user_id) REFERENCES users (id);


-- Quests: every quest has exactly one owner.

ALTER TABLE quest
    ADD COLUMN user_id BIGINT NULL;

UPDATE quest
SET user_id = (SELECT MIN(id) FROM users);

ALTER TABLE quest
    MODIFY user_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_quest_user
        FOREIGN KEY (user_id) REFERENCES users (id);
