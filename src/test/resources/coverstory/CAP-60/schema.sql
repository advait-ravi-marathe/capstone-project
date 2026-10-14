CREATE SCHEMA IF NOT EXISTS cap60_harness;
CREATE TABLE cap60_harness.journey (
  journey_id UUID PRIMARY KEY,
  owner_id UUID NOT NULL,
  version BIGINT NOT NULL CHECK (version > 0),
  current_step TEXT NOT NULL,
  UNIQUE (journey_id, owner_id)
);
CREATE TABLE cap60_harness.domain_effect (
  effect_id UUID PRIMARY KEY,
  journey_id UUID NOT NULL,
  owner_id UUID NOT NULL,
  revision BIGINT NOT NULL CHECK (revision > 0),
  causation_id UUID NOT NULL,
  FOREIGN KEY (journey_id, owner_id) REFERENCES cap60_harness.journey(journey_id, owner_id),
  UNIQUE (journey_id, revision),
  UNIQUE (journey_id, causation_id)
);
CREATE TABLE cap60_harness.command_key (
  journey_id UUID NOT NULL REFERENCES cap60_harness.journey(journey_id),
  key_hash BYTEA NOT NULL CHECK (octet_length(key_hash) = 32),
  payload_hash BYTEA NOT NULL CHECK (octet_length(payload_hash) = 32),
  stored_status INTEGER NOT NULL CHECK (stored_status BETWEEN 200 AND 499),
  PRIMARY KEY (journey_id, key_hash)
);
CREATE TABLE cap60_harness.audit_event (
  audit_id UUID PRIMARY KEY,
  journey_id UUID NOT NULL REFERENCES cap60_harness.journey(journey_id),
  causation_id UUID NOT NULL,
  event_code TEXT NOT NULL,
  UNIQUE (journey_id, causation_id, event_code)
);
CREATE TABLE cap60_harness.step_event (
  step_id UUID PRIMARY KEY,
  journey_id UUID NOT NULL REFERENCES cap60_harness.journey(journey_id),
  sequence BIGINT NOT NULL CHECK (sequence > 0),
  causation_id UUID NOT NULL,
  event_code TEXT NOT NULL,
  UNIQUE (journey_id, sequence),
  UNIQUE (journey_id, causation_id, event_code)
);
