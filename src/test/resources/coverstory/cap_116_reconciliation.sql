-- Instructor-only synthetic facts. No provider, charge, refund or product write is represented.
CREATE SCHEMA IF NOT EXISTS cap116_harness;
CREATE TABLE cap116_harness.accepted_offer_revision (
  scenario_id TEXT NOT NULL,
  owner_id UUID NOT NULL,
  application_id UUID NOT NULL,
  offer_id UUID NOT NULL,
  revision TEXT NOT NULL CHECK (revision ~ '^[a-z0-9-]{1,32}$'),
  content_sha256 CHAR(64) NOT NULL CHECK (content_sha256 ~ '^[0-9a-f]{64}$'),
  total_minor BIGINT NOT NULL CHECK (total_minor >= 0),
  currency CHAR(3) NOT NULL,
  accepted BOOLEAN NOT NULL,
  PRIMARY KEY (scenario_id, application_id),
  UNIQUE (scenario_id, offer_id, revision, content_sha256)
);
CREATE TABLE cap116_harness.effect_observation (
  observation_id UUID PRIMARY KEY,
  scenario_id TEXT NOT NULL,
  effect_id UUID NULL,
  owner_id UUID NOT NULL,
  application_id UUID NOT NULL,
  provider_reference TEXT NOT NULL,
  effect_kind TEXT NOT NULL CHECK (effect_kind IN ('CAPTURE','REFUND')),
  amount_minor BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  offer_id UUID NOT NULL,
  revision TEXT NOT NULL,
  content_sha256 CHAR(64) NOT NULL CHECK (content_sha256 ~ '^[0-9a-f]{64}$'),
  authoritative BOOLEAN NOT NULL
);
CREATE TABLE cap116_harness.pending_allocation (
  allocation_id UUID PRIMARY KEY,
  scenario_id TEXT NOT NULL,
  owner_id UUID NOT NULL,
  application_id UUID NOT NULL,
  idempotency_key TEXT NOT NULL,
  offer_id UUID NOT NULL,
  revision TEXT NOT NULL,
  content_sha256 CHAR(64) NOT NULL CHECK (content_sha256 ~ '^[0-9a-f]{64}$'),
  allocation_kind TEXT NOT NULL CHECK (allocation_kind IN ('ADDITIONAL_PAYMENT','REFUND')),
  status TEXT NOT NULL CHECK (status IN ('UNSTARTED','IN_FLIGHT','UNKNOWN','CONFIRMED','CANCELLED')),
  amount_minor BIGINT NOT NULL CHECK (amount_minor >= 0),
  currency CHAR(3) NOT NULL,
  UNIQUE (owner_id, application_id, idempotency_key)
);
