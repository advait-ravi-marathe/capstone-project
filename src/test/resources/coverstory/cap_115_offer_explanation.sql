-- Instructor-only synthetic offer/revision/evidence and explicit human choice tables.
CREATE SCHEMA IF NOT EXISTS cap115_harness;
CREATE TABLE cap115_harness.offer_snapshot (
  offer_id UUID PRIMARY KEY,
  snapshot_role TEXT NOT NULL CHECK(snapshot_role IN ('ORIGINAL','REVISED')),
  owner_id UUID NOT NULL,
  revision TEXT NOT NULL CHECK(revision ~ '^[a-z0-9-]{1,32}$'),
  content_sha256 CHAR(64) NOT NULL CHECK(content_sha256 ~ '^[0-9a-f]{64}$'),
  amount_minor BIGINT NOT NULL CHECK(amount_minor >= 0),
  currency CHAR(3) NOT NULL CHECK(currency='INR'),
  term_codes TEXT[] NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  UNIQUE(offer_id,owner_id,revision)
);
CREATE TABLE cap115_harness.authorized_source (
  owner_id UUID NOT NULL,
  source_id TEXT NOT NULL CHECK(source_id ~ '^[a-z0-9-]{1,40}$'),
  version TEXT NOT NULL CHECK(version ~ '^[a-z0-9._-]{1,32}$'),
  PRIMARY KEY(owner_id,source_id,version)
);
CREATE TABLE cap115_harness.explicit_quote_choice (
  choice_id UUID PRIMARY KEY,
  owner_id UUID NOT NULL,
  offer_id UUID NOT NULL,
  revision TEXT NOT NULL,
  content_sha256 CHAR(64) NOT NULL CHECK(content_sha256 ~ '^[0-9a-f]{64}$'),
  choice TEXT NOT NULL CHECK(choice IN ('ACCEPT_OFFER','DECLINE_OFFER')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(owner_id,offer_id,revision),
  FOREIGN KEY(offer_id,owner_id,revision) REFERENCES cap115_harness.offer_snapshot(offer_id,owner_id,revision)
);
