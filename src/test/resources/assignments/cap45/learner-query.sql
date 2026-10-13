-- LEARNER TODO: return one row per accepted in-window event with its attempts.
-- JDBC placeholders, in order: owner_id, start_utc, end_utc.
-- Join on BOTH owner_id and event_id; retain no-attempt events; don't window attempts.
-- Columns: owner_id, event_id, amount, attempt_count, last_attempt_at, has_attempt.
SELECT NULL::TEXT AS owner_id,
       'TODO'::TEXT AS event_id,
       NULL::NUMERIC(19, 4) AS amount,
       NULL::BIGINT AS attempt_count,
       NULL::TIMESTAMPTZ AS last_attempt_at,
       NULL::BOOLEAN AS has_attempt
WHERE ?::TEXT IS NOT NULL
  AND ?::TIMESTAMPTZ IS NOT NULL
  AND ?::TIMESTAMPTZ IS NOT NULL;
