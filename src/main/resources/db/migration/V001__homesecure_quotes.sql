-- Day 8 learner migration scaffold.
-- TODO: add primary, foreign, unique, NOT NULL, and ordering constraints
-- required by docs/reboot/contracts/day-08.md. The tables below intentionally
-- omit those invariants so the real PostgreSQL acceptance tests fail until
-- the learner completes this migration.

CREATE TABLE journeys (
    journey_id TEXT
);

CREATE TABLE quote_versions (
    journey_id TEXT,
    quote_version_id TEXT,
    product_code TEXT,
    product_version TEXT,
    rate_version TEXT,
    rebuild_amount_rupees NUMERIC(14, 2),
    coverage_code TEXT,
    currency TEXT,
    annual_premium_rupees NUMERIC(14, 2),
    created_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ
);

CREATE TABLE selections (
    journey_id TEXT,
    quote_version_id TEXT,
    selection_order INTEGER
);
