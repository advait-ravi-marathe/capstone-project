CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS application;

CREATE TABLE payments.payment_checkpoint (
    owner_id uuid PRIMARY KEY,
    last_sequence integer NOT NULL CHECK (last_sequence >= 0)
);

CREATE TABLE payments.mock_capture_effect (
    event_id text PRIMARY KEY,
    owner_id uuid NOT NULL,
    sequence integer NOT NULL CHECK (sequence > 0),
    application_id uuid NOT NULL,
    quote_id text NOT NULL,
    amount_minor bigint NOT NULL CHECK (amount_minor >= 0),
    currency char(3) NOT NULL,
    UNIQUE (owner_id, sequence)
);

CREATE TABLE application.onboarding_case (
    application_id uuid PRIMARY KEY,
    owner_id uuid NOT NULL,
    quote_id text NOT NULL,
    state text NOT NULL CHECK (state IN ('AWAITING_PAYMENT', 'ONBOARDING')),
    payment_event_id text NOT NULL UNIQUE
);

CREATE TABLE application.onboarding_timeline (
    timeline_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application.onboarding_case(application_id),
    owner_id uuid NOT NULL,
    payment_event_id text NOT NULL UNIQUE,
    from_state text NOT NULL,
    to_state text NOT NULL CHECK (to_state = 'ONBOARDING')
);
