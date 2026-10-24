DROP TABLE IF EXISTS training_policy_event;
CREATE TABLE training_policy_event (
    owner_id text NOT NULL,
    policy_id text NOT NULL,
    revision integer NOT NULL CHECK (revision > 0),
    event_at timestamptz NOT NULL,
    payload text NOT NULL,
    PRIMARY KEY (owner_id, policy_id, revision)
);
INSERT INTO training_policy_event(owner_id, policy_id, revision, event_at, payload)
SELECT 'owner-' || lpad((o % 40)::text, 2, '0'),
       'policy-' || lpad(p::text, 4, '0'),
       r,
       timestamptz '2025-01-01 00:00:00+00' + (r || ' minutes')::interval,
       repeat('x', 80)
FROM generate_series(0, 39) AS o
CROSS JOIN generate_series(1, 30) AS p
CROSS JOIN generate_series(1, 8) AS r;
CREATE INDEX training_policy_event_owner_policy_revision_idx
    ON training_policy_event(owner_id, policy_id, revision DESC);
ANALYZE training_policy_event;
