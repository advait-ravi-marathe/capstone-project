DROP TABLE IF EXISTS financial_events;
CREATE TABLE financial_events (
    owner_id VARCHAR(64) NOT NULL,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    currency CHAR(3) NOT NULL CHECK (currency = 'USD'),
    amount NUMERIC(19, 4) NOT NULL,
    PRIMARY KEY (owner_id, event_id)
);
