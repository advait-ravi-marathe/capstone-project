-- CAP-642 matched local synthetic experiment. Bind :owner_id and :shortlist_id.
-- Record EXPLAIN (ANALYZE, BUFFERS), rows scanned/returned, errors and warm/cold state.
-- Baseline owner-scoped deterministic page (do not remove owner predicate).
EXPLAIN (ANALYZE, BUFFERS, TIMING)
SELECT revision, checkpoint_id, parent_revision, scenario_id, slot_refs, source_digest_set_sha256
FROM shortlist.checkpoint_revision
WHERE owner_id = :owner_id AND shortlist_id = :shortlist_id AND revision < :before_revision
ORDER BY revision DESC
LIMIT 20;

-- Candidate query is identical; compare under the separately declared candidate
-- index/settings. At most three current-source refs are fetched through Catalog's
-- public batch port after this page; this query never joins Catalog-owned tables.
EXPLAIN (ANALYZE, BUFFERS, TIMING)
SELECT revision, checkpoint_id, parent_revision, scenario_id, slot_refs, source_digest_set_sha256
FROM shortlist.checkpoint_revision
WHERE owner_id = :owner_id AND shortlist_id = :shortlist_id AND revision < :before_revision
ORDER BY revision DESC
LIMIT 20;
