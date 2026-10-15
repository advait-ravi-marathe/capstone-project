-- Instructor-supplied storage boundary for protected synthetic payloads.
-- Apply after cap78_purchase.sql; key material is held by a separate provider.
CREATE TABLE IF NOT EXISTS application.application_revision (
    application_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    revision BIGINT NOT NULL CHECK (revision > 0),
    quote_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (application_id, revision),
    UNIQUE (application_id, owner_id, revision),
    FOREIGN KEY (application_id, owner_id) REFERENCES application.application_record(application_id, owner_id)
);

CREATE TABLE IF NOT EXISTS application.protected_payload_envelope (
    envelope_id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    application_revision BIGINT NOT NULL CHECK (application_revision > 0),
    payload_kind TEXT NOT NULL CHECK (payload_kind IN ('APPLICATION_ANSWERS', 'HEALTH_DECLARATIONS')),
    schema_version INTEGER NOT NULL CHECK (schema_version > 0),
    cipher_format_version INTEGER NOT NULL CHECK (cipher_format_version = 1),
    key_id TEXT NOT NULL CHECK (length(key_id) BETWEEN 1 AND 100),
    nonce BYTEA NOT NULL CHECK (octet_length(nonce) = 12),
    ciphertext BYTEA NOT NULL CHECK (octet_length(ciphertext) >= 16),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (application_id, application_revision, payload_kind),
    FOREIGN KEY (application_id, owner_id, application_revision)
        REFERENCES application.application_revision(application_id, owner_id, revision)
);

CREATE OR REPLACE FUNCTION application.reject_protected_envelope_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'protected payload envelope is immutable';
END;
$$;
DROP TRIGGER IF EXISTS protected_envelope_append_only ON application.protected_payload_envelope;
CREATE TRIGGER protected_envelope_append_only
    BEFORE UPDATE OR DELETE ON application.protected_payload_envelope
    FOR EACH ROW EXECUTE FUNCTION application.reject_protected_envelope_mutation();
