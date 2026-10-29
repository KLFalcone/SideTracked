-- ============================================================
-- V5: REMOVE LEGACY RABBIT HOLE TABLES
-- ============================================================
--
-- V3 copied every legacy rabbit hole into topics and
-- user_rabbit_holes (most progressed row per title, journal notes
-- included). Nothing has read or written these tables since, so
-- all they still hold is the duplicate rows and outdated copies of
-- topic content.
--
-- Verified before this migration was written: all 35 legacy rows
-- (5 distinct titles) are represented in user_rabbit_holes with an
-- equal or more progressed status and the same journal notes.
--
-- Child tables go first because of their foreign keys.

DROP TABLE IF EXISTS rabbit_hole_tags;
DROP TABLE IF EXISTS rabbit_hole_sources;
DROP TABLE IF EXISTS rabbit_holes;
