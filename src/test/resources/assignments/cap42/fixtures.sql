INSERT INTO financial_events (owner_id, event_id, status, occurred_at, currency, amount) VALUES
  ('owner-A', 'a-lower', 'ACCEPTED', TIMESTAMPTZ '2026-10-13 00:00:00+00', 'USD', 100.00),
  ('owner-A', 'a-negative', 'ACCEPTED', TIMESTAMPTZ '2026-10-13 12:00:00+00', 'USD', -20.00),
  ('owner-A', 'a-rejected', 'REJECTED', TIMESTAMPTZ '2026-10-13 13:00:00+00', 'USD', 999.00),
  ('owner-A', 'a-upper', 'ACCEPTED', TIMESTAMPTZ '2026-10-14 00:00:00+00', 'USD', 50.00),
  ('owner-B', 'b-in-range', 'ACCEPTED', TIMESTAMPTZ '2026-10-13 10:00:00+00', 'USD', 500.00);
