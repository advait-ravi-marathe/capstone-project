CREATE TABLE training_policy_lifecycle (
  policy_id TEXT PRIMARY KEY,
  owner_id TEXT NOT NULL,
  journey_id TEXT NOT NULL,
  quote_version_id TEXT NOT NULL,
  product_version TEXT NOT NULL,
  rate_version TEXT NOT NULL,
  annual_premium_rupees NUMERIC(12,2) NOT NULL CHECK (annual_premium_rupees >= 0),
  currency CHAR(3) NOT NULL CHECK (currency = 'INR'),
  effective_date DATE NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('ISSUED','CANCEL_SCHEDULED','CANCELLED')),
  version BIGINT NOT NULL CHECK (version >= 1),
  cancellation_effective_date DATE,
  UNIQUE(owner_id, journey_id, quote_version_id)
);
CREATE TABLE training_policy_lifecycle_event (
  policy_id TEXT NOT NULL REFERENCES training_policy_lifecycle(policy_id),
  version BIGINT NOT NULL CHECK (version >= 1),
  event_type TEXT NOT NULL CHECK (event_type IN ('ISSUED','ENDORSED','CANCEL_SCHEDULED','CANCELLED')),
  effective_date DATE NOT NULL,
  change_code TEXT,
  training_rebuild_amount_rupees NUMERIC(12,2),
  PRIMARY KEY(policy_id, version)
);
CREATE TABLE training_policy_idempotency (
  owner_id TEXT NOT NULL,
  client_request_id TEXT NOT NULL,
  request_fingerprint TEXT NOT NULL,
  policy_id TEXT NOT NULL REFERENCES training_policy_lifecycle(policy_id),
  PRIMARY KEY(owner_id, client_request_id)
);
