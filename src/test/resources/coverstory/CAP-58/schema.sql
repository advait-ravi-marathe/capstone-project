CREATE SCHEMA cap58_harness;
CREATE TABLE cap58_harness.receipt (
  event_id UUID NOT NULL,
  owner_hash BYTEA NOT NULL CHECK (octet_length(owner_hash)=32),
  payload_hash BYTEA NOT NULL CHECK (octet_length(payload_hash)=32),
  session_hash BYTEA NOT NULL CHECK (octet_length(session_hash)=32),
  event_type TEXT NOT NULL CHECK (event_type IN ('PAGE_VIEW','CONTROL_CLICK')),
  page_id TEXT NOT NULL,
  target_id TEXT,
  duration_ms INTEGER CHECK (duration_ms BETWEEN 0 AND 300000),
  received_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY(event_id, owner_hash)
);
