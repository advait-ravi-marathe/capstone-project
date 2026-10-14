DROP TABLE IF EXISTS catalog_import_evidence;
DROP TABLE IF EXISTS catalog_date_spine;
CREATE TABLE catalog_import_evidence (
    editor_fixture text NOT NULL,
    source_id text NOT NULL,
    publication_state text NOT NULL CHECK (publication_state IN ('PUBLISHED','DRAFT')),
    base_amount_minor bigint,
    currency char(3) NOT NULL CHECK (currency = 'INR'),
    captured_at timestamptz NOT NULL,
    PRIMARY KEY (editor_fixture, source_id, captured_at)
);
CREATE TABLE catalog_date_spine(day_utc date PRIMARY KEY);
