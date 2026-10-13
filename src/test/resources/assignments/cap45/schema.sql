DROP TABLE IF EXISTS delivery_attempts;
DROP TABLE IF EXISTS events;
CREATE TABLE events (
    owner_id TEXT NOT NULL,
    event_id TEXT NOT NULL,
    status TEXT NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (owner_id, event_id)
);
CREATE TABLE delivery_attempts (
    owner_id TEXT NOT NULL,
    event_id TEXT NOT NULL,
    attempt_id TEXT NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (owner_id, event_id, attempt_id)
);
