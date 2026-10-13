INSERT INTO events (owner_id, event_id, status, amount, occurred_at) VALUES
  ('A', 'e1', 'accepted', 100, TIMESTAMPTZ '2026-10-13 00:00:00+00'),
  ('A', 'e2', 'accepted', -20, TIMESTAMPTZ '2026-10-13 12:00:00+00'),
  ('A', 'e3', 'rejected', 999, TIMESTAMPTZ '2026-10-13 13:00:00+00'),
  ('A', 'e4', 'accepted', 50, TIMESTAMPTZ '2026-10-14 00:00:00+00'),
  ('B', 'e1', 'accepted', 500, TIMESTAMPTZ '2026-10-13 10:00:00+00');

INSERT INTO delivery_attempts (owner_id, event_id, attempt_id, attempted_at) VALUES
  ('A', 'e1', 't1', TIMESTAMPTZ '2026-10-13 10:00:00+00'),
  ('A', 'e1', 't2', TIMESTAMPTZ '2026-10-13 11:00:00+00'),
  ('B', 'e1', 't3', TIMESTAMPTZ '2026-10-13 12:00:00+00');
