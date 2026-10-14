CREATE SCHEMA IF NOT EXISTS payments;
CREATE TABLE payments.reconciliation_checkpoint (
    owner_id uuid PRIMARY KEY,
    updated_at timestamptz NOT NULL,
    event_id text NOT NULL
);
CREATE TABLE payments.reconciliation_observation (
    owner_id uuid NOT NULL,
    updated_at timestamptz NOT NULL,
    event_id text NOT NULL,
    payload_hash text NOT NULL,
    PRIMARY KEY (owner_id, updated_at, event_id)
);
