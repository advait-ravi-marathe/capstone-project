DROP TABLE IF EXISTS catalog_import_attempt;
DROP TABLE IF EXISTS catalog_import_evidence;
CREATE TABLE catalog_import_evidence (
    editor_fixture text NOT NULL,
    source_id text NOT NULL,
    source_version text NOT NULL,
    publication_state text NOT NULL CHECK (publication_state IN ('PUBLISHED','DRAFT')),
    base_amount_minor bigint,
    currency char(3) NOT NULL CHECK (currency = 'INR'),
    captured_at timestamptz NOT NULL,
    PRIMARY KEY (editor_fixture, source_id, source_version)
);
CREATE TABLE catalog_import_attempt (
    editor_fixture text NOT NULL,
    source_id text NOT NULL,
    source_version text NOT NULL,
    attempt_number integer NOT NULL CHECK (attempt_number > 0),
    attempted_at timestamptz NOT NULL,
    PRIMARY KEY (editor_fixture, source_id, source_version, attempt_number),
    FOREIGN KEY (editor_fixture, source_id, source_version)
      REFERENCES catalog_import_evidence (editor_fixture, source_id, source_version)
);
