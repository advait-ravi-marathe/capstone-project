-- Initial instructor scaffolding from the approved setup brief. Product behavior stays learner work.
-- Catalog schema intentionally starts with versioned opaque references, no insurer is integrated.
CREATE TABLE IF NOT EXISTS catalog.offer_version (
    offer_id UUID NOT NULL,
    catalog_version TEXT NOT NULL,
    display_name TEXT NOT NULL,
    category TEXT NOT NULL,
    PRIMARY KEY (offer_id, catalog_version)
);

CREATE TABLE IF NOT EXISTS shortlist.saved_offer (
    owner_id UUID NOT NULL,
    offer_id UUID NOT NULL,
    catalog_version TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (owner_id, offer_id)
);

CREATE TABLE IF NOT EXISTS application.journey (
    journey_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    application_id UUID NOT NULL UNIQUE,
    revision BIGINT NOT NULL CHECK (revision > 0),
    quote_id UUID NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('DRAFT', 'SUBMITTED_PENDING_DISPATCH', 'CANCELLED')),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS journey_owner_updated_idx ON application.journey(owner_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS payments.payment_intent (
    payment_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    application_id UUID NOT NULL,
    application_revision BIGINT NOT NULL CHECK (application_revision > 0),
    idempotency_key_hash BYTEA NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor >= 0),
    currency CHAR(3) NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('RECORDED', 'DISPATCH_PENDING', 'CANCELLED', 'RECONCILIATION_REQUIRED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (owner_id, idempotency_key_hash)
);

-- Local mock confirmation replay exercises payment/onboarding atomicity only.
-- No external payment provider or insurer dispatch is connected to these rows.
CREATE TABLE IF NOT EXISTS payments.mock_confirmation_feed (
    event_id TEXT NOT NULL,
    owner_id UUID NOT NULL,
    sequence INTEGER NOT NULL CHECK (sequence > 0),
    application_id UUID NOT NULL,
    quote_id TEXT NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    confirmed BOOLEAN NOT NULL,
    PRIMARY KEY (owner_id, event_id),
    UNIQUE (owner_id, sequence)
);
CREATE TABLE IF NOT EXISTS payments.payment_checkpoint (
    owner_id UUID PRIMARY KEY,
    last_sequence INTEGER NOT NULL CHECK (last_sequence >= 0)
);
CREATE TABLE IF NOT EXISTS payments.mock_capture_effect (
    event_id TEXT PRIMARY KEY,
    owner_id UUID NOT NULL,
    sequence INTEGER NOT NULL,
    application_id UUID NOT NULL,
    quote_id TEXT NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    UNIQUE (owner_id, sequence),
    FOREIGN KEY (owner_id, sequence) REFERENCES payments.mock_confirmation_feed(owner_id, sequence)
);

CREATE TABLE IF NOT EXISTS application.onboarding_case (
    application_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    quote_id TEXT NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('AWAITING_PAYMENT', 'ONBOARDING')),
    payment_event_id TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS application.onboarding_timeline (
    timeline_id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES application.onboarding_case(application_id),
    owner_id UUID NOT NULL,
    payment_event_id TEXT NOT NULL,
    from_state TEXT NOT NULL CHECK (from_state = 'AWAITING_PAYMENT'),
    to_state TEXT NOT NULL CHECK (to_state = 'ONBOARDING'),
    UNIQUE (application_id, payment_event_id)
);

CREATE TABLE IF NOT EXISTS payments.payment_journal (
    journal_id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments.payment_intent(payment_id),
    entry_type TEXT NOT NULL CHECK (entry_type IN ('PAYMENT_RECORDED', 'REFUND_RECORDED', 'CANCELLATION_RECORDED', 'RECONCILIATION_ADJUSTMENT')),
    amount_minor BIGINT NOT NULL,
    currency CHAR(3) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION payments.reject_journal_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'payment journal is append-only';
END;
$$;
DROP TRIGGER IF EXISTS payment_journal_append_only ON payments.payment_journal;
CREATE TRIGGER payment_journal_append_only
    BEFORE UPDATE OR DELETE ON payments.payment_journal
    FOR EACH ROW EXECUTE FUNCTION payments.reject_journal_mutation();

CREATE TABLE IF NOT EXISTS policy.dispatch (
    dispatch_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    application_id UUID NOT NULL,
    application_revision BIGINT NOT NULL CHECK (application_revision > 0),
    payment_id UUID NOT NULL REFERENCES payments.payment_intent(payment_id),
    state TEXT NOT NULL CHECK (state IN ('PENDING', 'CONFIRMED', 'FAILED', 'RECONCILIATION_REQUIRED')),
    provider_reference TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS support.support_case (
    case_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    category TEXT NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('OPEN', 'IN_PROGRESS', 'RESOLVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Atomic business history is separate from best-effort click/page-view analytics.
-- Do not put raw consumer/health fields, tokens, URLs, or arbitrary properties here.
CREATE TABLE IF NOT EXISTS business_audit.event (
    event_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    aggregate_type TEXT NOT NULL CHECK (aggregate_type IN ('APPLICATION', 'PAYMENT', 'DISPATCH', 'SUPPORT_CASE')),
    aggregate_id UUID NOT NULL,
    event_type TEXT NOT NULL CHECK (event_type IN ('APPLICATION_SUBMITTED', 'SIMULATED_PAYMENT_RECORDED', 'DISPATCH_REQUESTED', 'DISPATCH_CONFIRMED', 'DISPATCH_FAILED', 'PAYMENT_CANCELLED', 'RECONCILIATION_REQUIRED', 'SUPPORT_CASE_OPENED')),
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS analytics.behavior_event (
    event_id UUID PRIMARY KEY,
    event_name TEXT NOT NULL CHECK (event_name IN ('catalog_viewed', 'shortlist_changed', 'application_started', 'support_opened')),
    screen_key TEXT NOT NULL CHECK (screen_key IN ('catalog', 'shortlist', 'application', 'support')),
    session_scope_hash BYTEA NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

-- Minimal screen telemetry receipts are best effort and are not business audit or journey state.
-- Owner/session identifiers are keyed hashes. Never add raw payload, PII, health data, URL, token,
-- client timestamps, or unbounded client fields to this table.
CREATE TABLE IF NOT EXISTS analytics.minimal_event_receipt (
    owner_hash BYTEA NOT NULL CHECK (octet_length(owner_hash) = 32),
    event_id UUID NOT NULL,
    payload_hash BYTEA NOT NULL CHECK (octet_length(payload_hash) = 32),
    session_scope_hash BYTEA NOT NULL CHECK (octet_length(session_scope_hash) = 32),
    event_type TEXT NOT NULL CHECK (event_type IN ('PAGE_VIEW', 'CONTROL_CLICK')),
    page_id TEXT NOT NULL CHECK (page_id IN ('health_discovery', 'compare', 'quote_review', 'home', 'checkout', 'onboarding', 'final_review')),
    step_code TEXT CHECK (step_code IS NULL OR step_code IN ('DISCOVER', 'COMPARE', 'QUOTE_REVIEW', 'ONBOARDING', 'FINAL_REVIEW')),
    target_id TEXT CHECK (target_id IS NULL OR target_id IN ('compare_plan', 'save_draft')),
    duration_ms INTEGER CHECK (duration_ms BETWEEN 0 AND 300000),
    server_received_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (owner_hash, event_id),
    CHECK ((event_type = 'PAGE_VIEW' AND target_id IS NULL) OR
           (event_type = 'CONTROL_CLICK' AND target_id IS NOT NULL))
);
CREATE INDEX IF NOT EXISTS minimal_event_receipt_received_at_idx
    ON analytics.minimal_event_receipt(owner_hash, server_received_at DESC);
