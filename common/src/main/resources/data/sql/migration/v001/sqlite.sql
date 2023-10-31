-- SQLite Conversion from V1 -> V2

-- Changes Needed:
-- 1. Create Version Table

-- Journey Database Version Tracker

CREATE TABLE journey_db_version (
    db_version INT
);
CREATE INDEX journey_db_version_idx ON journey_db_version (
    db_version
);