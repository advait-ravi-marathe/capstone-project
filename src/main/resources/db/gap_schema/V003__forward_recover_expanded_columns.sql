-- Forward repair after an unsafe drift removed an expanded column; legacy values remain authoritative.
ALTER TABLE gap_backfill_item ADD COLUMN IF NOT EXISTS normalized_value text;
ALTER TABLE gap_backfill_item ADD COLUMN IF NOT EXISTS normalization_version smallint;
ALTER TABLE gap_backfill_item ADD COLUMN IF NOT EXISTS update_count integer NOT NULL DEFAULT 0;
UPDATE gap_backfill_item
   SET normalized_value=lower(trim(legacy_value)), normalization_version=1, update_count=1
 WHERE normalized_value IS NULL;
