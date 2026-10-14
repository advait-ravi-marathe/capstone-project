-- Learner TODO: return exactly one row for each qualifying published evidence
-- record. Preserve nullable amounts and evidence with no attempts. Bind editor,
-- from_utc, and to_utc; the capture window is half-open. Aggregate attempts by
-- the full (editor_fixture, source_id, source_version) key before joining.
SELECT NULL::text AS source_id,
       NULL::bigint AS base_amount_minor,
       NULL::integer AS attempt_count,
       NULL::timestamptz AS last_attempt_at,
       NULL::boolean AS has_attempt
FROM (SELECT ?::text AS editor_fixture, ?::timestamptz AS from_utc, ?::timestamptz AS to_utc) AS requested_window
WHERE FALSE;
