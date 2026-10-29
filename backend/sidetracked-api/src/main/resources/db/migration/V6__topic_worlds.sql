-- ============================================================
-- V6: TOPICS CAN BELONG TO SEVERAL WORLDS
-- ============================================================
--
-- topics.world now holds the slug of the topic's primary World
-- (e.g. "atomic-age"). topic_worlds lists the other Worlds the topic
-- connects to, so a discovery can be found from any of them.
--
-- Curated topics are rewritten with the new values at startup by
-- CuratedTopicCatalog, so no data needs converting here.

CREATE TABLE topic_worlds (
    topic_id    BIGINT       NOT NULL,
    world_slug  VARCHAR(100) NOT NULL,
    world_order INT          NOT NULL,
    PRIMARY KEY (topic_id, world_order),
    CONSTRAINT fk_topic_worlds_topic
        FOREIGN KEY (topic_id) REFERENCES topics (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
