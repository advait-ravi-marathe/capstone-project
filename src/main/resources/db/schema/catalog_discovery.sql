-- Additive metadata for the public synthetic discovery projection. Source terms
-- remain in the immutable catalog version table; publication status is derived.
CREATE TABLE IF NOT EXISTS catalog.health_offer_discovery_metadata (
    product_id UUID NOT NULL,
    version_id UUID NOT NULL,
    category TEXT NOT NULL CHECK (category = 'health'),
    scenario_id TEXT NOT NULL CHECK (length(btrim(scenario_id)) > 0),
    source_id TEXT NOT NULL CHECK (length(btrim(source_id)) > 0),
    evidence_status TEXT NOT NULL CHECK (evidence_status IN ('SYNTHETIC', 'SOURCE_EVIDENCE')),
    display_name TEXT NOT NULL CHECK (length(btrim(display_name)) > 0),
    PRIMARY KEY (product_id, version_id),
    FOREIGN KEY (product_id, version_id)
      REFERENCES catalog.health_offer_version(product_id, version_id)
);

CREATE OR REPLACE VIEW catalog.health_offer_discovery_export AS
SELECT m.version_id, m.category, m.scenario_id, m.source_id,
       v.source_version, m.evidence_status, m.display_name,
       CASE WHEN a.version_id = v.version_id THEN 'PUBLISHED' ELSE 'RETIRED' END AS publication_status
  FROM catalog.health_offer_discovery_metadata m
  JOIN catalog.health_offer_version v USING (product_id, version_id)
  LEFT JOIN catalog.active_health_offer a ON a.product_id = m.product_id;
