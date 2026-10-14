-- CAP-78 instructor schema. No credentials, real provider, or consumer answers.
ALTER TABLE application.journey ADD COLUMN IF NOT EXISTS current_step TEXT NOT NULL DEFAULT 'DISCOVER';
ALTER TABLE application.journey DROP CONSTRAINT IF EXISTS journey_status_check;
ALTER TABLE application.journey ADD CONSTRAINT journey_status_check
    CHECK (status IN ('DRAFT', 'WAITING', 'SUBMITTED_PENDING_DISPATCH', 'CANCELLED'));
ALTER TABLE application.journey DROP CONSTRAINT IF EXISTS journey_step_check;
ALTER TABLE application.journey ADD CONSTRAINT journey_step_check
    CHECK (current_step IN ('DISCOVER', 'COMPARE', 'QUOTE_REVIEW', 'PAYMENT_PENDING', 'ONBOARDING'));
ALTER TABLE application.journey ADD CONSTRAINT journey_owner_identity_unique UNIQUE (journey_id, owner_id);

CREATE TABLE application.application_record (
    application_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    revision BIGINT NOT NULL CHECK (revision > 0),
    quote_id UUID NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('DRAFT', 'AWAITING_PAYMENT', 'ONBOARDING', 'SUBMITTED_PENDING_DISPATCH')),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (application_id, owner_id)
);

CREATE TABLE catalog.approved_quote (
    quote_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    quote_version TEXT NOT NULL,
    source_illustration_id UUID NOT NULL,
    source_state TEXT NOT NULL CHECK (source_state IN ('ACTIVE', 'WITHDRAWN')),
    total_minor BIGINT NOT NULL CHECK (total_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    terms_hash CHAR(64) NOT NULL,
    quote_revision BIGINT NOT NULL CHECK (quote_revision > 0),
    UNIQUE (quote_id, owner_id)
);

CREATE TABLE application.quote_validation (
    validation_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    journey_id UUID NOT NULL,
    quote_id UUID NOT NULL,
    quote_revision BIGINT NOT NULL,
    accepted_total_minor BIGINT NOT NULL CHECK (accepted_total_minor > 0),
    terms_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted BOOLEAN NOT NULL,
    FOREIGN KEY (journey_id, owner_id) REFERENCES application.journey(journey_id, owner_id),
    FOREIGN KEY (quote_id, owner_id) REFERENCES catalog.approved_quote(quote_id, owner_id)
);

CREATE TABLE payments.mock_order (
    order_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    journey_id UUID NOT NULL,
    application_id UUID NOT NULL,
    quote_id UUID NOT NULL,
    quote_revision BIGINT NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    terms_hash CHAR(64) NOT NULL,
    idempotency_key_hash BYTEA NOT NULL CHECK (octet_length(idempotency_key_hash) = 32),
    request_hash BYTEA NOT NULL CHECK (octet_length(request_hash) = 32),
    operation_id UUID NOT NULL UNIQUE,
    state TEXT NOT NULL CHECK (state IN ('PENDING', 'FAILED', 'UNKNOWN', 'SUCCEEDED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (owner_id, idempotency_key_hash),
    FOREIGN KEY (journey_id, owner_id) REFERENCES application.journey(journey_id, owner_id),
    FOREIGN KEY (application_id, owner_id) REFERENCES application.application_record(application_id, owner_id),
    FOREIGN KEY (quote_id, owner_id) REFERENCES catalog.approved_quote(quote_id, owner_id)
);

CREATE UNIQUE INDEX mock_order_one_pending_per_application
    ON payments.mock_order(application_id) WHERE state IN ('PENDING', 'UNKNOWN');

CREATE TABLE payments.payment_attempt (
    attempt_id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES payments.mock_order(order_id),
    owner_id UUID NOT NULL,
    state TEXT NOT NULL CHECK (state IN ('PENDING', 'UNKNOWN', 'FAILED', 'SUCCEEDED')),
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments.outbox_intent (
    outbox_id UUID PRIMARY KEY,
    operation_id UUID NOT NULL REFERENCES payments.mock_order(operation_id),
    attempt_id UUID NOT NULL REFERENCES payments.payment_attempt(attempt_id),
    event_type TEXT NOT NULL CHECK (event_type = 'MOCK_PAYMENT_PENDING'),
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'DELIVERED', 'RETRY')),
    request_id TEXT NOT NULL CHECK (length(request_id) BETWEEN 1 AND 100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (operation_id, event_type)
);

CREATE TABLE business_audit.transaction_audit_event (
    event_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    aggregate_type TEXT NOT NULL CHECK (aggregate_type = 'PAYMENT'),
    aggregate_id UUID NOT NULL REFERENCES payments.payment_attempt(attempt_id),
    aggregate_sequence BIGINT NOT NULL CHECK (aggregate_sequence > 0),
    journey_id UUID NOT NULL REFERENCES application.journey(journey_id),
    operation_reference UUID NOT NULL,
    event_code TEXT NOT NULL CHECK (event_code = 'MOCK_PAYMENT_PENDING'),
    prior_state TEXT NOT NULL CHECK (prior_state = 'NONE'),
    new_state TEXT NOT NULL CHECK (new_state = 'PENDING'),
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    request_id TEXT NOT NULL CHECK (length(request_id) BETWEEN 1 AND 100),
    occurred_at TIMESTAMPTZ NOT NULL,
    UNIQUE (aggregate_type, aggregate_id, aggregate_sequence),
    UNIQUE (operation_reference, event_code)
);

CREATE TABLE application.journey_step_event (
    step_event_id UUID PRIMARY KEY,
    journey_id UUID NOT NULL REFERENCES application.journey(journey_id),
    sequence BIGINT NOT NULL CHECK (sequence > 0),
    event_code TEXT NOT NULL CHECK (event_code = 'MOCK_PAYMENT_PENDING'),
    from_step TEXT NOT NULL CHECK (from_step = 'QUOTE_REVIEW'),
    to_step TEXT NOT NULL CHECK (to_step = 'PAYMENT_PENDING'),
    actor_ref UUID NOT NULL,
    domain_record_type TEXT NOT NULL CHECK (domain_record_type = 'PAYMENT_ATTEMPT'),
    domain_record_id UUID NOT NULL REFERENCES payments.payment_attempt(attempt_id),
    domain_revision BIGINT NOT NULL CHECK (domain_revision > 0),
    causation_id UUID NOT NULL,
    request_id TEXT NOT NULL CHECK (length(request_id) BETWEEN 1 AND 100),
    occurred_at TIMESTAMPTZ NOT NULL,
    UNIQUE (journey_id, sequence),
    UNIQUE (journey_id, causation_id, event_code)
);
