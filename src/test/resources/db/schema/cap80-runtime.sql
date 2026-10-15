CREATE SCHEMA payments;
CREATE SCHEMA application;
CREATE TABLE payments.mock_confirmation_feed (
  event_id TEXT NOT NULL, owner_id UUID NOT NULL, sequence INTEGER NOT NULL,
  application_id UUID NOT NULL, quote_id TEXT NOT NULL, amount_minor BIGINT NOT NULL,
  currency CHAR(3) NOT NULL, confirmed BOOLEAN NOT NULL,
  PRIMARY KEY(owner_id,event_id), UNIQUE(owner_id,sequence)
);
CREATE TABLE payments.payment_checkpoint(owner_id UUID PRIMARY KEY,last_sequence INTEGER NOT NULL);
CREATE TABLE payments.mock_capture_effect(
  event_id TEXT PRIMARY KEY, owner_id UUID NOT NULL, sequence INTEGER NOT NULL,
  application_id UUID NOT NULL, quote_id TEXT NOT NULL, amount_minor BIGINT NOT NULL,
  currency CHAR(3) NOT NULL, UNIQUE(owner_id,sequence),
  FOREIGN KEY(owner_id,sequence) REFERENCES payments.mock_confirmation_feed(owner_id,sequence)
);
CREATE TABLE application.onboarding_case(
  application_id UUID PRIMARY KEY, owner_id UUID NOT NULL, quote_id TEXT NOT NULL,
  state TEXT NOT NULL, payment_event_id TEXT NOT NULL
);
CREATE TABLE application.onboarding_timeline(
  timeline_id UUID PRIMARY KEY, application_id UUID NOT NULL REFERENCES application.onboarding_case(application_id),
  owner_id UUID NOT NULL, payment_event_id TEXT NOT NULL, from_state TEXT NOT NULL, to_state TEXT NOT NULL,
  UNIQUE(application_id,payment_event_id)
);
