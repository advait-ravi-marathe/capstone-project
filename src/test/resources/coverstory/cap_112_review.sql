-- Instructor-only synthetic schema. No insurer callback or production API is wired.
CREATE SCHEMA IF NOT EXISTS cap112_harness;
CREATE TABLE cap112_harness.application_review_state (
  application_id UUID PRIMARY KEY, owner_id UUID NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('SUBMITTED','REVIEW','NEEDS_INFORMATION')),
  version BIGINT NOT NULL CHECK (version > 0), UNIQUE(application_id,owner_id)
);
CREATE TABLE cap112_harness.source_event (
  owner_id UUID NOT NULL, event_id TEXT NOT NULL, application_id UUID NOT NULL,
  version BIGINT NOT NULL CHECK(version > 0), received_status TEXT NOT NULL CHECK(received_status IN ('REVIEW','NEEDS_INFORMATION','SUBMITTED')),
  outcome TEXT NOT NULL CHECK(outcome IN ('APPLIED','STALE_RECORDED')),
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(owner_id,event_id),
  FOREIGN KEY(application_id,owner_id) REFERENCES cap112_harness.application_review_state(application_id,owner_id)
);
CREATE TABLE cap112_harness.review_timeline (
  timeline_id UUID PRIMARY KEY, owner_id UUID NOT NULL, application_id UUID NOT NULL,
  version BIGINT NOT NULL, status TEXT NOT NULL CHECK(status IN ('REVIEW','NEEDS_INFORMATION')), source_event_id TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(owner_id,source_event_id),
  FOREIGN KEY(owner_id,source_event_id) REFERENCES cap112_harness.source_event(owner_id,event_id)
);
CREATE TABLE cap112_harness.safe_diagnostic (
  request_id UUID PRIMARY KEY, route_code TEXT NOT NULL CHECK(route_code IN ('INSURER_REVIEW_CALLBACK')),
  outcome TEXT NOT NULL CHECK(outcome IN ('APPLIED','DUPLICATE','STALE_RECORDED','OWNER_NOT_FOUND','PROVIDER_FAILURE')),
  duration_millis INTEGER NOT NULL CHECK(duration_millis BETWEEN 0 AND 60000),
  received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
