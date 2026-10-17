-- Candidate Shortlist-owned schema for CAP-634/635/641/642.
-- Store only owner-scoped references, version/digest metadata and safe outcomes.
-- No private answers, health text, free-form notes, tokens or copied source payloads.
CREATE TABLE IF NOT EXISTS shortlist.checkpoint_revision (
    owner_id UUID NOT NULL,
    shortlist_id UUID NOT NULL,
    revision BIGINT NOT NULL CHECK (revision > 0),
    parent_revision BIGINT,
    checkpoint_id UUID NOT NULL,
    scenario_id TEXT NOT NULL CHECK (length(btrim(scenario_id)) > 0),
    slot_refs JSONB NOT NULL CHECK (jsonb_typeof(slot_refs) = 'array' AND jsonb_array_length(slot_refs) BETWEEN 1 AND 3),
    source_digest_set_sha256 BYTEA NOT NULL CHECK (octet_length(source_digest_set_sha256) = 32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (owner_id, shortlist_id, revision),
    UNIQUE (checkpoint_id),
    FOREIGN KEY (owner_id, shortlist_id, parent_revision)
        REFERENCES shortlist.checkpoint_revision(owner_id, shortlist_id, revision)
        DEFERRABLE INITIALLY DEFERRED,
    CHECK ((revision = 1 AND parent_revision IS NULL) OR (revision > 1 AND parent_revision = revision - 1))
);

CREATE TABLE IF NOT EXISTS shortlist.checkpoint_head (
    owner_id UUID NOT NULL,
    shortlist_id UUID NOT NULL,
    revision BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (owner_id, shortlist_id),
    FOREIGN KEY (owner_id, shortlist_id, revision)
        REFERENCES shortlist.checkpoint_revision(owner_id, shortlist_id, revision)
        DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX IF NOT EXISTS checkpoint_revision_history_idx
    ON shortlist.checkpoint_revision(owner_id, shortlist_id, revision DESC);

CREATE TABLE IF NOT EXISTS shortlist.checkpoint_operation (
    owner_id UUID NOT NULL,
    shortlist_id UUID NOT NULL,
    operation_kind TEXT NOT NULL CHECK (operation_kind = 'CHECKPOINT_REFRESH_ACCEPT'),
    operation_key_hash BYTEA NOT NULL CHECK (octet_length(operation_key_hash) = 32),
    request_sha256 BYTEA NOT NULL CHECK (octet_length(request_sha256) = 32),
    result_revision BIGINT,
    result_code TEXT NOT NULL CHECK (result_code IN ('IN_PROGRESS','ACCEPTED','CONFLICT','REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (owner_id, shortlist_id, operation_kind, operation_key_hash),
    FOREIGN KEY (owner_id, shortlist_id, result_revision)
        REFERENCES shortlist.checkpoint_revision(owner_id, shortlist_id, revision)
        DEFERRABLE INITIALLY DEFERRED
);
