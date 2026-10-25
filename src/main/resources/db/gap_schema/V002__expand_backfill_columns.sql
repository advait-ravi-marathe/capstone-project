ALTER TABLE gap_backfill_item ADD COLUMN normalized_value text;
ALTER TABLE gap_backfill_item ADD COLUMN normalization_version smallint;
ALTER TABLE gap_backfill_item ADD COLUMN update_count integer NOT NULL DEFAULT 0;

CREATE TABLE gap_backfill_checkpoint (
    job_id text PRIMARY KEY,
    last_item_id integer NOT NULL CHECK (last_item_id >= 0)
);
