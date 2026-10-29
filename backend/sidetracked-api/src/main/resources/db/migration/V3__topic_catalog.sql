-- ============================================================
-- V3: SHARED TOPIC CATALOG + PERSONAL RABBIT HOLES
-- ============================================================
--
-- Before: every click of Get SideTracked inserted a new row into
-- rabbit_holes, mixing shared content (title, links, tags) with
-- personal progress (status, journal note). Nothing prevented
-- duplicates, so one topic could appear dozens of times.
--
-- After:
--   topics             one row per Rabbit Hole, shared by everyone
--   user_rabbit_holes  one row per user per topic (enforced),
--                      holding that user's status and journal note
--
-- Existing data is COPIED. When several legacy rows share a title,
-- the most progressed one is kept (COMPLETED > EXPLORING > SAVED >
-- DISCOVERED) and the earliest discovery date is used. The legacy
-- rabbit_holes tables are left untouched for a later cleanup.


-- ------------------------------------------------------------
-- NEW TABLES
-- ------------------------------------------------------------

-- source_provider + external_id identify where a topic came from
-- (CURATED + a title slug today; e.g. WIKIPEDIA + a page id later)
-- and stop the same topic from ever being added twice.
CREATE TABLE topics (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    source_provider VARCHAR(50)   NOT NULL,
    external_id     VARCHAR(255)  NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    world           VARCHAR(255)  DEFAULT NULL,
    hook            VARCHAR(2000) DEFAULT NULL,
    research_prompt VARCHAR(2000) DEFAULT NULL,
    xp              INT           NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_topics_source UNIQUE (source_provider, external_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE topic_tags (
    topic_id  BIGINT       NOT NULL,
    tag       VARCHAR(255) DEFAULT NULL,
    tag_order INT          NOT NULL,
    PRIMARY KEY (topic_id, tag_order),
    CONSTRAINT fk_topic_tags_topic
        FOREIGN KEY (topic_id) REFERENCES topics (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE topic_sources (
    topic_id     BIGINT        NOT NULL,
    label        VARCHAR(255)  DEFAULT NULL,
    source_type  VARCHAR(255)  DEFAULT NULL,
    url          VARCHAR(2000) DEFAULT NULL,
    source_order INT           NOT NULL,
    PRIMARY KEY (topic_id, source_order),
    CONSTRAINT fk_topic_sources_topic
        FOREIGN KEY (topic_id) REFERENCES topics (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_rabbit_holes (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    user_id       BIGINT        NOT NULL,
    topic_id      BIGINT        NOT NULL,
    status        ENUM ('COMPLETED', 'DISCOVERED', 'EXPLORING', 'SAVED') NOT NULL,
    reflection    VARCHAR(2000) DEFAULT NULL,
    discovered_at DATETIME(6)   NOT NULL,
    completed_at  DATETIME(6)   DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_rabbit_holes_user_topic UNIQUE (user_id, topic_id),
    CONSTRAINT fk_user_rabbit_holes_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_rabbit_holes_topic
        FOREIGN KEY (topic_id) REFERENCES topics (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- ------------------------------------------------------------
-- COPY LEGACY DATA
-- ------------------------------------------------------------

-- Legacy rabbit holes belong to the local user. Create that user
-- only if rabbit holes exist but V2 had nothing to adopt.
INSERT INTO users (display_name, created_at)
SELECT 'Local Developer', NOW(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users)
  AND EXISTS (SELECT 1 FROM rabbit_holes);

-- The single best legacy row for each distinct title.
CREATE TEMPORARY TABLE legacy_best AS
SELECT ranked.id,
       ranked.title,
       ranked.world,
       ranked.hook,
       ranked.research_prompt,
       ranked.xp,
       ranked.status,
       ranked.reflection,
       ranked.first_seen
FROM (
    SELECT rh.*,
           ROW_NUMBER() OVER (
               PARTITION BY rh.title
               ORDER BY CASE rh.status
                            WHEN 'COMPLETED' THEN 1
                            WHEN 'EXPLORING' THEN 2
                            WHEN 'SAVED' THEN 3
                            ELSE 4
                        END,
                        rh.id
           ) AS rn,
           MIN(rh.discovered_at) OVER (PARTITION BY rh.title) AS first_seen
    FROM rabbit_holes rh
    WHERE rh.title IS NOT NULL
) ranked
WHERE ranked.rn = 1;

-- external_id: lowercase title with every run of other characters
-- turned into one dash, e.g. "THE WOW! SIGNAL" -> "the-wow-signal".
-- Topic.slugify() in Java produces the same value.
INSERT INTO topics (source_provider, external_id, title, world, hook,
                    research_prompt, xp, created_at)
SELECT 'CURATED',
       TRIM(BOTH '-' FROM REGEXP_REPLACE(LOWER(lb.title), '[^a-z0-9]+', '-')),
       lb.title,
       lb.world,
       lb.hook,
       lb.research_prompt,
       lb.xp,
       COALESCE(lb.first_seen, NOW(6))
FROM legacy_best lb;

INSERT INTO topic_tags (topic_id, tag, tag_order)
SELECT t.id, rht.tag, rht.tag_order
FROM legacy_best lb
JOIN topics t ON t.title = lb.title
JOIN rabbit_hole_tags rht ON rht.rabbit_hole_id = lb.id;

INSERT INTO topic_sources (topic_id, label, source_type, url, source_order)
SELECT t.id, rhs.label, rhs.source_type, rhs.url, rhs.source_order
FROM legacy_best lb
JOIN topics t ON t.title = lb.title
JOIN rabbit_hole_sources rhs ON rhs.rabbit_hole_id = lb.id;

INSERT INTO user_rabbit_holes (user_id, topic_id, status, reflection,
                               discovered_at, completed_at)
SELECT (SELECT MIN(id) FROM users),
       t.id,
       COALESCE(lb.status, 'DISCOVERED'),
       lb.reflection,
       COALESCE(lb.first_seen, NOW(6)),
       NULL
FROM legacy_best lb
JOIN topics t ON t.title = lb.title;

DROP TEMPORARY TABLE legacy_best;
