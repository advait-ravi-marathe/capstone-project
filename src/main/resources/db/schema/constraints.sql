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
