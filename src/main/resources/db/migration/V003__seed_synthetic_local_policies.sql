-- Stable IDs and fabricated values only. This is a versioned seed, not a reset.
INSERT INTO training_policy_lifecycle (
    policy_id, owner_id, journey_id, quote_version_id, product_version, rate_version,
    annual_premium_rupees, currency, effective_date, status, version, cancellation_effective_date
) VALUES
    ('policy-demo-1', 'owner-a', 'journey-demo-a', 'quote-demo-a', 'home-secure-v1',
     'home-secure-rate-v1', 125.00, 'INR', DATE '2026-12-01', 'ISSUED', 1, NULL),
    ('policy-renewal-a', 'owner-a', 'journey-renewal-a', 'quote-renewal-a', 'home-secure-v1',
     'home-secure-rate-v1', 125.00, 'INR', DATE '2026-01-15', 'ISSUED', 3, NULL),
    ('policy-renewal-b', 'owner-b', 'journey-renewal-b', 'quote-renewal-b', 'home-secure-v1',
     'home-secure-rate-v1', 180.00, 'INR', DATE '2026-02-01', 'ISSUED', 1, NULL);

INSERT INTO training_policy_lifecycle_event (policy_id, version, event_type, effective_date)
VALUES ('policy-demo-1', 1, 'ISSUED', DATE '2026-12-01'),
       ('policy-renewal-a', 1, 'ISSUED', DATE '2026-01-15'),
       ('policy-renewal-a', 2, 'ENDORSED', DATE '2026-03-01'),
       ('policy-renewal-a', 3, 'ENDORSED', DATE '2026-04-01'),
       ('policy-renewal-b', 1, 'ISSUED', DATE '2026-02-01');
