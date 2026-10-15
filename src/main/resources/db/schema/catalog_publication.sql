-- CAP-57 catalog source history and published-selection export. The source
-- rows are immutable. Publication only selects a prepared (product, version).
CREATE TABLE IF NOT EXISTS catalog.health_offer_version (
    product_id UUID NOT NULL,
    version_id UUID NOT NULL,
    source_version TEXT NOT NULL CHECK (length(btrim(source_version)) > 0),
    restrictions JSONB NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor >= 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
    content_sha256 BYTEA NOT NULL CHECK (octet_length(content_sha256) = 32),
    PRIMARY KEY (product_id, version_id)
);

CREATE TABLE IF NOT EXISTS catalog.active_health_offer (
    product_id UUID PRIMARY KEY,
    version_id UUID NOT NULL,
    published_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (product_id, version_id)
        REFERENCES catalog.health_offer_version(product_id, version_id)
);

-- Required business audit for a catalog publication. It contains only the
-- synthetic editor and immutable source/version/hash references; it has no
-- consumer or payload fields and is written atomically with the active row.
CREATE TABLE IF NOT EXISTS catalog.publication_audit_event (
    audit_id UUID PRIMARY KEY,
    editor_id UUID NOT NULL,
    event_code TEXT NOT NULL CHECK (event_code = 'CATALOG_VERSION_PUBLISHED'),
    product_id UUID NOT NULL,
    version_id UUID NOT NULL,
    source_version TEXT NOT NULL,
    content_sha256 BYTEA NOT NULL CHECK (octet_length(content_sha256) = 32),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (product_id, version_id, event_code),
    FOREIGN KEY (product_id, version_id)
        REFERENCES catalog.health_offer_version(product_id, version_id)
);

CREATE OR REPLACE FUNCTION catalog.reject_health_offer_mutation() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'catalog source versions are immutable';
END;
$$;
DROP TRIGGER IF EXISTS health_offer_version_immutable ON catalog.health_offer_version;
CREATE TRIGGER health_offer_version_immutable
    BEFORE UPDATE OR DELETE ON catalog.health_offer_version
    FOR EACH ROW EXECUTE FUNCTION catalog.reject_health_offer_mutation();

-- Only safe, stable fields are exportable; restrictions and consumer fields
-- are not part of the public projection.
CREATE OR REPLACE VIEW catalog.published_health_offer_export AS
SELECT v.product_id, v.version_id, encode(v.content_sha256, 'hex') AS content_sha256,
       v.amount_minor, v.currency
FROM catalog.active_health_offer a
JOIN catalog.health_offer_version v USING (product_id, version_id);
