-- LEARNER TODO: replace this shape-only query with the bounded source contract.
-- Parameters are editor_fixture, from_utc and to_utc in that order.
SELECT 0::bigint AS published_count, 0::numeric AS known_base_total_minor
WHERE ?::text IS NOT NULL AND ?::timestamptz IS NOT NULL AND ?::timestamptz IS NOT NULL;
