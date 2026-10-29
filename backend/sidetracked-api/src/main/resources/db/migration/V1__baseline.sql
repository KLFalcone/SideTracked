-- ============================================================
-- V1: BASELINE
-- ============================================================
--
-- The schema exactly as Hibernate's ddl-auto=update had built it
-- before Flyway was introduced (taken from the 2026-10-28 backup).
--
-- Existing databases are marked as already being at V1
-- (spring.flyway.baseline-on-migrate), so this file only runs
-- on a brand-new, empty database.
--
-- Structure only. No personal or development data belongs here.

CREATE TABLE players (
    id                   BIGINT NOT NULL AUTO_INCREMENT,
    encounters_completed INT    NOT NULL,
    quests_completed     INT    NOT NULL,
    xp                   INT    NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE quest (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    completed   BIT(1)       NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    priority    VARCHAR(255) DEFAULT NULL,
    title       VARCHAR(255) DEFAULT NULL,
    type        VARCHAR(255) DEFAULT NULL,
    xp          INT          NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE rabbit_holes (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    discovered_at   DATETIME(6)   DEFAULT NULL,
    hook            VARCHAR(2000) DEFAULT NULL,
    research_prompt VARCHAR(2000) DEFAULT NULL,
    status          ENUM ('COMPLETED', 'DISCOVERED', 'EXPLORING', 'SAVED') DEFAULT NULL,
    title           VARCHAR(255)  DEFAULT NULL,
    world           VARCHAR(255)  DEFAULT NULL,
    xp              INT           NOT NULL,
    reflection      VARCHAR(2000) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE rabbit_hole_sources (
    rabbit_hole_id BIGINT        NOT NULL,
    label          VARCHAR(255)  DEFAULT NULL,
    source_type    VARCHAR(255)  DEFAULT NULL,
    url            VARCHAR(2000) DEFAULT NULL,
    source_order   INT           NOT NULL,
    PRIMARY KEY (rabbit_hole_id, source_order),
    CONSTRAINT fk_rabbit_hole_sources_rabbit_hole
        FOREIGN KEY (rabbit_hole_id) REFERENCES rabbit_holes (id),
    CONSTRAINT rabbit_hole_sources_chk_1 CHECK (source_order >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE rabbit_hole_tags (
    rabbit_hole_id BIGINT       NOT NULL,
    tag            VARCHAR(255) DEFAULT NULL,
    tag_order      INT          NOT NULL,
    PRIMARY KEY (rabbit_hole_id, tag_order),
    CONSTRAINT fk_rabbit_hole_tags_rabbit_hole
        FOREIGN KEY (rabbit_hole_id) REFERENCES rabbit_holes (id),
    CONSTRAINT rabbit_hole_tags_chk_1 CHECK (tag_order >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
