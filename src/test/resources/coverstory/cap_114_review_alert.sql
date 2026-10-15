-- Instructor-only synthetic alert window storage. Not a production Support schema.
CREATE SCHEMA IF NOT EXISTS cap114_harness;
CREATE TABLE cap114_harness.window_sample (
  window_id TEXT PRIMARY KEY CHECK (window_id ~ '^[a-z0-9-]{1,40}$'),
  eligible_requests INTEGER NOT NULL CHECK (eligible_requests >= 0),
  error_requests INTEGER NOT NULL CHECK (error_requests >= 0 AND error_requests <= eligible_requests),
  needs_information_count INTEGER NOT NULL CHECK (needs_information_count >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE cap114_harness.alert_state (
  singleton BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK(singleton),
  state TEXT NOT NULL CHECK(state IN ('NORMAL','PENDING','FIRING','RESOLVED')),
  qualifying_windows INTEGER NOT NULL CHECK(qualifying_windows >= 0),
  firing_transitions INTEGER NOT NULL CHECK(firing_transitions BETWEEN 0 AND 1),
  resolution_transitions INTEGER NOT NULL CHECK(resolution_transitions BETWEEN 0 AND 1),
  last_window_id TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO cap114_harness.alert_state(singleton,state,qualifying_windows,firing_transitions,resolution_transitions)
VALUES(TRUE,'NORMAL',0,0,0);
CREATE TABLE cap114_harness.alert_transition (
  transition_id BIGSERIAL PRIMARY KEY,
  from_state TEXT NOT NULL CHECK(from_state IN ('NORMAL','PENDING','FIRING','RESOLVED')),
  to_state TEXT NOT NULL CHECK(to_state IN ('NORMAL','PENDING','FIRING','RESOLVED')),
  window_id TEXT NOT NULL UNIQUE REFERENCES cap114_harness.window_sample(window_id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
