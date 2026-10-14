# CAP-58 — Validate allowlisted analytics batches

**Saved contract:** `validateAnalyticsBatch(byte[] utf8Body, TrustedAnalyticsContext)` validates a strictly bounded, low-sensitivity telemetry batch. The learner validator remains TODO; no HTTP ingestion or persistence service is claimed.

## Instructor fixtures and checks

- `fixtures/coverstory/CAP-58/synthetic-v1.json` supplies synthetic PAGE_VIEW and CONTROL_CLICK examples plus invalid target, boolean duration, and sensitive-property cases.
- `CAP58InstructorInfrastructureTest` independently checks the allowlists, exact integer duration boundaries, unknown/sensitive-field rejection, and 8192-byte UTF-8 boundary math. It does not call the learner seam.
- `CAP58AcceptanceTest` exercises the learner seam for allowlisted events, server receive time, byte overflow, and disallowed collection. It currently fails at `TODO(CAP-58)`.

The saved contract also requires HTTP 202 discard behavior, 400/413/429 mapping, authenticated journey-link checks, durable event-ID idempotency, safe logging, and a bounded browser retry queue. Those are integration work and remain deferred. `clientTime` never establishes server order. Synthetic fixtures do not contain consumer data.

## Paths and commands

- Learner seam: `src/main/java/coverstory/modules/analytics/api/CAP58Seam.java`
- Instructor oracle: `src/test/java/coverstory/modules/analytics/CAP58InstructorInfrastructureTest.java`
- Learner tests: `src/test/java/coverstory/modules/analytics/CAP58AcceptanceTest.java`
- Instructor command: `mvn -Dtest=CAP58InstructorInfrastructureTest test`
- Learner command: `mvn -Plearner-tests -Dtest=CAP58AcceptanceTest test`
