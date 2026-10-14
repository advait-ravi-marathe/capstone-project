CREATE SCHEMA IF NOT EXISTS payments;
CREATE TABLE payments.staged_callback (
  row_id uuid PRIMARY KEY,
  owner_id uuid NOT NULL,
  batch_id uuid NOT NULL,
  payload_hash text NOT NULL
);
CREATE TABLE payments.callback_outcome (
  row_id uuid PRIMARY KEY REFERENCES payments.staged_callback(row_id),
  owner_id uuid NOT NULL,
  batch_id uuid NOT NULL,
  classification text NOT NULL CHECK (classification IN ('INSERTED','DUPLICATE','INVALID'))
);
CREATE TABLE payments.payment_effect (
  effect_id uuid PRIMARY KEY,
  owner_id uuid NOT NULL,
  source_row_id uuid NOT NULL,
  amount_minor bigint NOT NULL CHECK (amount_minor >= 0),
  UNIQUE(owner_id, source_row_id)
);
