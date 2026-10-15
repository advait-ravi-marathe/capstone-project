CREATE SCHEMA IF NOT EXISTS cap111_harness;

-- Synthetic only. These tables support an independent instructor HTTP/transaction witness;
-- production submission and dispatcher tables/routes are deliberately not registered.
CREATE TABLE cap111_harness.review_facts (
    application_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    reviewed_revision BIGINT NOT NULL CHECK (reviewed_revision > 0),
    quote_id UUID NOT NULL,
    configuration_hash CHAR(64) NOT NULL,
    terms_hash CHAR(64) NOT NULL,
    current_configuration_hash CHAR(64) NOT NULL,
    current_terms_hash CHAR(64) NOT NULL,
    required_fields_complete BOOLEAN NOT NULL,
    disclosures_complete BOOLEAN NOT NULL,
    extraction_confirmed BOOLEAN NOT NULL,
    payment_reconciled BOOLEAN NOT NULL,
    refund_reconciled BOOLEAN NOT NULL,
    UNIQUE (application_id, owner_id),
    FOREIGN KEY (application_id, owner_id) REFERENCES application.application_record(application_id, owner_id)
);

CREATE TABLE cap111_harness.submission_intent (
    intent_id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    reviewed_revision BIGINT NOT NULL CHECK (reviewed_revision > 0),
    quote_id UUID NOT NULL,
    configuration_hash CHAR(64) NOT NULL,
    terms_hash CHAR(64) NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('SUBMISSION_PENDING', 'SUBMITTED', 'CANCELLED', 'RECONCILIATION_REQUIRED')),
    external_reference TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (application_id, reviewed_revision),
    FOREIGN KEY (application_id, owner_id) REFERENCES application.application_record(application_id, owner_id)
);

CREATE TABLE cap111_harness.dispatch_intent (
    dispatch_id UUID PRIMARY KEY,
    submission_intent_id UUID NOT NULL UNIQUE REFERENCES cap111_harness.submission_intent(intent_id),
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'CLAIMED', 'CANCELLED', 'UNKNOWN', 'ACKNOWLEDGED')),
    claim_id UUID UNIQUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cap111_harness.idempotency_receipt (
    owner_id UUID NOT NULL,
    application_id UUID NOT NULL,
    key_hash BYTEA NOT NULL CHECK (octet_length(key_hash) = 32),
    request_hash BYTEA NOT NULL CHECK (octet_length(request_hash) = 32),
    intent_id UUID NOT NULL REFERENCES cap111_harness.submission_intent(intent_id),
    response_status SMALLINT NOT NULL CHECK (response_status = 202),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (owner_id, application_id, key_hash)
);
