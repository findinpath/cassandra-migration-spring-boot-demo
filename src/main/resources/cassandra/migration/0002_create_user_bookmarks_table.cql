CREATE TABLE IF NOT EXISTS user_bookmarks (
    user_id UUID,
    timestamp TIMEUUID,
    url VARCHAR,
    PRIMARY KEY (user_id, timestamp)
    ) WITH CLUSTERING ORDER BY (timestamp DESC);