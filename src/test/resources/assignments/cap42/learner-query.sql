-- LEARNER TODO: replace with one parameterized PostgreSQL SELECT.
-- JDBC placeholders, in order: owner_id, from_utc, to_utc.
-- Return exactly one row: accepted_count and signed_total.
-- Use the half-open UTC interval [from_utc, to_utc); empty totals are decimal 0.
-- Preserve signed amounts. Fixture currency is uniformly USD and event IDs are unique.
SELECT NULL::BIGINT AS accepted_count,
       NULL::NUMERIC(19, 4) AS signed_total
WHERE ?::TEXT IS NOT NULL
  AND ?::TIMESTAMPTZ IS NOT NULL
  AND ?::TIMESTAMPTZ IS NOT NULL;
