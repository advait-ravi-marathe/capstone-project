-- Learner TODO: count published evidence records and sum known minor-unit
-- amounts once each. Count an evidence record as confirmed when at least one
-- successful import-attempt receipt exists for the same editor and source.
SELECT NULL::bigint AS published_count,
       NULL::numeric AS known_base_total_minor,
       NULL::bigint AS confirmed_import_count
FROM (SELECT ?::text AS editor_fixture,
             ?::timestamptz AS from_utc,
             ?::timestamptz AS to_utc) AS requested_window
WHERE FALSE;
