-- Additive synthetic event-log-backed durable-work materialization.
-- Existing lifecycle events are the durable intent; these tables do not alter
-- the Day 35 issuance transaction or claim a simultaneous outbox insert.
CREATE TABLE training_issuance_work (
    policy_id TEXT NOT NULL,
    source_version BIGINT NOT NULL CHECK (source_version >= 1),
    event_type TEXT NOT NULL CHECK (event_type = 'ISSUED'),
    effective_date DATE NOT NULL,
    envelope_schema_version INTEGER NOT NULL CHECK (envelope_schema_version = 1),
    captured_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    acknowledged_at TIMESTAMPTZ,
    PRIMARY KEY (policy_id, source_version),
    FOREIGN KEY (policy_id, source_version)
        REFERENCES training_policy_lifecycle_event (policy_id, version)
);

CREATE TABLE training_issuance_delivery_receipt (
    policy_id TEXT NOT NULL,
    source_version BIGINT NOT NULL CHECK (source_version >= 1),
    event_type TEXT NOT NULL CHECK (event_type = 'ISSUED'),
    effective_date DATE NOT NULL,
    envelope_schema_version INTEGER NOT NULL CHECK (envelope_schema_version = 1),
    received_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (policy_id, source_version),
    FOREIGN KEY (policy_id, source_version)
        REFERENCES training_issuance_work (policy_id, source_version)
);

CREATE INDEX training_issuance_work_pending_idx
    ON training_issuance_work (policy_id, source_version)
    WHERE acknowledged_at IS NULL;
