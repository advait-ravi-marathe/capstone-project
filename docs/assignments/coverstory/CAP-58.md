# CAP-58 — Validate allowlisted analytics batches

**Saved contract:** `validateAnalyticsBatch(byte[] utf8Body, TrustedAnalyticsContext)` validates a strictly bounded, low-sensitivity telemetry batch. The learner validator remains TODO. A real Spring HTTP boundary now delegates to an Analytics-owned ingestion port; the default application adapter fails closed with 503 until a validator and durable receipt store are supplied.

## Instructor fixtures and checks

- `fixtures/coverstory/CAP-58/synthetic-v1.json` supplies synthetic PAGE_VIEW and CONTROL_CLICK examples plus invalid target, boolean duration, and sensitive-property cases.
- `CAP58InstructorInfrastructureTest` independently checks the allowlists, exact integer duration boundaries, unknown/sensitive-field rejection, and 8192-byte UTF-8 boundary math. The test-only ingress harness covers bounded batches and safe status mapping.
- `CAP58AnalyticsHttpIntegrationTest` exercises the actual Spring MVC route for anonymous 401, hard 8192-byte 413, sanitized validation errors, accepted/replayed 202 receipts and changed-payload 409 through that test-only adapter.
- `CAP58PostgresAcceptanceTest` sends requests through the same Spring MVC controller to an isolated embedded PostgreSQL receipt adapter. It verifies first receipt/replay/conflict, immutable allowlisted columns, an exact 8,192-byte body with ten events, atomic rejection of eleven events, UTF-8 overflow, duration boundaries, and sensitive-field rejection. The schema and adapter are test-only and contain no raw payload column.
- `web/src/coverstory/analytics/BoundedTelemetryQueue.ts` and `npm run test:cap58-browser-queue` exercise a memory-only queue capped at 20 events and three attempts per batch. The Chromium witness confirms dropped events do not block a checkout action.
- `CAP58AcceptanceTest` exercises the learner seam for allowlisted events, server receive time, byte overflow, and disallowed collection. It currently fails at `TODO(CAP-58)`.

The PostgreSQL oracle is test-only: the application still has no production analytics validator or durable receipt adapter. Persistent validated journey-link authorization, operational 429 behavior and retention remain deferred. The working HTTP path therefore fails closed by default. `clientTime` never establishes server order. Synthetic fixtures do not contain consumer data.

## Paths and commands

- Learner seam: `src/main/java/coverstory/modules/analytics/api/CAP58Seam.java`
- Instructor oracle: `src/test/java/coverstory/modules/analytics/CAP58InstructorInfrastructureTest.java`
- Learner tests: `src/test/java/coverstory/modules/analytics/CAP58AcceptanceTest.java`
- Instructor commands: `mvn -Dtest=CAP58InstructorInfrastructureTest,CAP58AnalyticsHttpIntegrationTest,CAP58PostgresAcceptanceTest test`; `cd web && npm run build && npm run test:cap58-browser-queue`
- Learner command: `mvn -Plearner-tests -Dtest=CAP58AcceptanceTest test`
