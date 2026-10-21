-- Reversible operator control metadata for retained synthetic issuance work.
-- Existing source envelopes and delivery receipts remain immutable.
ALTER TABLE training_issuance_work
    ADD COLUMN quarantined_at TIMESTAMPTZ,
    ADD COLUMN quarantine_reason TEXT,
    ADD COLUMN control_revision INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT training_issuance_work_control_revision_nonnegative
        CHECK (control_revision >= 0),
    ADD CONSTRAINT training_issuance_work_quarantine_pair
        CHECK ((quarantined_at IS NULL AND quarantine_reason IS NULL)
            OR (quarantined_at IS NOT NULL AND quarantine_reason IS NOT NULL AND control_revision >= 1 AND quarantine_reason IN
                ('OBSERVED_FAILURE','RECEIPT_CONFLICT','MANUAL_HOLD')));

CREATE TABLE training_issuance_recovery_history (
    policy_id TEXT NOT NULL,
    source_version BIGINT NOT NULL CHECK (source_version >= 1),
    control_revision INTEGER NOT NULL CHECK (control_revision >= 1),
    action TEXT NOT NULL CHECK (action IN ('QUARANTINE','RELEASE')),
    reason_code TEXT NOT NULL,
    decided_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (policy_id, source_version, control_revision),
    FOREIGN KEY (policy_id, source_version)
        REFERENCES training_issuance_work (policy_id, source_version),
    CONSTRAINT training_issuance_recovery_history_reason_by_action
        CHECK ((action = 'QUARANTINE' AND reason_code IN
                    ('OBSERVED_FAILURE','RECEIPT_CONFLICT','MANUAL_HOLD'))
            OR (action = 'RELEASE' AND reason_code = 'REVIEWED_FOR_RETRY'))
);

CREATE INDEX training_issuance_recovery_history_recent_idx
    ON training_issuance_recovery_history (policy_id, source_version, control_revision DESC);
