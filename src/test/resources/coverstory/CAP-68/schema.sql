CREATE SCHEMA IF NOT EXISTS cap68_harness;
CREATE TABLE cap68_harness.journey (
  journey_id UUID PRIMARY KEY,
  owner_id UUID NOT NULL,
  version BIGINT NOT NULL CHECK (version > 0),
  current_step TEXT NOT NULL,
  UNIQUE (journey_id, owner_id)
);
CREATE TABLE cap68_harness.shortlist_item (
  item_id UUID PRIMARY KEY,
  journey_id UUID NOT NULL,
  owner_id UUID NOT NULL,
  operation_id UUID NOT NULL UNIQUE,
  item_code TEXT NOT NULL,
  FOREIGN KEY (journey_id, owner_id) REFERENCES cap68_harness.journey(journey_id, owner_id)
);
CREATE TABLE cap68_harness.command_key (
  journey_id UUID NOT NULL,
  owner_id UUID NOT NULL,
  key_hash BYTEA NOT NULL CHECK (octet_length(key_hash) = 32),
  payload_hash BYTEA NOT NULL CHECK (octet_length(payload_hash) = 32),
  operation_id UUID NOT NULL,
  stored_status INTEGER NOT NULL CHECK (stored_status BETWEEN 200 AND 499),
  stored_version BIGINT NOT NULL CHECK (stored_version > 0),
  PRIMARY KEY (journey_id, key_hash),
  FOREIGN KEY (journey_id, owner_id) REFERENCES cap68_harness.journey(journey_id, owner_id)
);
CREATE TABLE cap68_harness.business_audit (
  audit_id UUID PRIMARY KEY,
  journey_id UUID NOT NULL,
  owner_id UUID NOT NULL,
  operation_id UUID NOT NULL,
  event_code TEXT NOT NULL,
  FOREIGN KEY (journey_id, owner_id) REFERENCES cap68_harness.journey(journey_id, owner_id),
  UNIQUE (journey_id, operation_id, event_code)
);
CREATE TABLE cap68_harness.step_event (
  step_event_id UUID PRIMARY KEY,
  journey_id UUID NOT NULL,
  owner_id UUID NOT NULL,
  version BIGINT NOT NULL CHECK (version > 0),
  operation_id UUID NOT NULL,
  event_code TEXT NOT NULL,
  FOREIGN KEY (journey_id, owner_id) REFERENCES cap68_harness.journey(journey_id, owner_id),
  UNIQUE (journey_id, version),
  UNIQUE (journey_id, operation_id, event_code)
);
