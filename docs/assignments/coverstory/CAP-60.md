# CAP-60 — Atomic journey advance race test

**Saved contract:** the learner writes a bounded two-connection concurrency and invariant test. The repository currently has no application command/service for this operation, so this starter supplies only a test-only PostgreSQL schema and synthetic race oracle; it does not simulate or claim an application endpoint.

## Instructor harness

- `src/test/resources/coverstory/CAP-60/schema.sql` defines synthetic journey, domain-effect, idempotency, business-audit, and step-event tables with owner, sequence, and causation uniqueness constraints.
- `CAP60InstructorInfrastructureTest` uses embedded PostgreSQL 14 and independent connections. It repeats a same-key race ten times, checks a competing expected-version-4 race, verifies each synthetic write-failure rollback, checks idempotency payload preservation, and rejects an owner-mismatched domain reference.
- `CAP60AcceptanceTest` is deliberately unfinished. Its three `TODO(CAP-60)` cases are where the learner should add executable concurrency and rollback assertions.

These are database-invariant exercises, not a wired product command, authenticated application flow, or live customer state. Barrier/future waits are bounded to five seconds. Fixture values are synthetic and have no payment or policy effects.

## Paths and commands

- Fixture: `fixtures/coverstory/CAP-60/synthetic-v1.json`
- SQL harness: `src/test/resources/coverstory/CAP-60/schema.sql`
- Instructor checks: `mvn -Dtest=CAP60InstructorInfrastructureTest test`
- Learner-owned contract tests: `mvn -Plearner-tests -Dtest=CAP60AcceptanceTest test`
