# CAP-60 — Atomic journey advance race test

**Saved contract:** the learner writes a bounded two-connection concurrency and invariant test. Instructor setup now supplies a callable test-only transaction command adapter plus disposable PostgreSQL schema, independent race witnesses and the saved failure controls. The repository still has no production application command/service or endpoint; none is claimed.

## Instructor harness

- `src/test/resources/coverstory/CAP-60/schema.sql` defines synthetic journey, domain-effect, idempotency, business-audit, and step-event tables with owner, sequence, and causation uniqueness constraints.
- `CAP60InstructorCommandAdapter` is a test-only callable command that executes the atomic reference transition on a JDBC transaction; `CAP60InstructorInfrastructureTest` runs it using independent connections. The oracle repeats a same-key race ten times, checks a competing expected-version-4 race, every synthetic write-failure rollback, changed payload conflict, a five-second missing-barrier failure, foreign-owner constraints, and that later analytics cannot change journey version.
- `CAP60AcceptanceTest` is deliberately unfinished. Its three `TODO(CAP-60)` cases are where the learner should add executable concurrency and rollback assertions.

The callable adapter exists only in test code. These are instructor reference-command and database-invariant exercises, not a wired product command, authenticated application flow, or live customer state. Barrier/future waits are bounded to five seconds. Fixture values are synthetic and have no payment or policy effects.

## Paths and commands

- Fixture: `fixtures/coverstory/CAP-60/synthetic-v1.json`
- SQL harness: `src/test/resources/coverstory/CAP-60/schema.sql`
- Instructor checks: `mvn -Dtest=CAP60InstructorInfrastructureTest test`
- Learner-owned contract tests: `mvn -Plearner-tests -Dtest=CAP60AcceptanceTest test`
