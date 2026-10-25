-- Contract only after the instructor/learner gate confirms no old reader remains.
ALTER TABLE gap_backfill_item DROP COLUMN legacy_value;
