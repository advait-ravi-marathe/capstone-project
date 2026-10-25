# Calendar study day 172 — GAP-SCHEMA-04: Interrupt and resume without skips or duplicates

**Date:** 2027-04-15  
**Budget:** 25/130/10 + shared 45 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use fresh bounded-runner invocations against only the persisted database cursor; this is not a claimed JVM-crash reproduction. Inject failures both during a row update and with a deferred database trigger on the checkpoint write after row updates in the learner implementation. Verify all rows and cursor roll back on fresh connections, then rerun the learner and compare every ordered row/cursor exactly.

## Source

[PostgreSQL 18 — UPDATE](https://www.postgresql.org/docs/18/sql-update.html)

## Acceptance

Learner-path row-update and checkpoint-write failures prove rollback across the row/cursor transaction; exact IDs, full values, version/update counts, cursor and wrong-row mutant are checked. Independent SQL fixture tests remain separate.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=ResumableBackfillAcceptanceTest test && ./mvnw -Dtest=SchemaEvolutionInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-04.json`. This is not learner completion until genuinely attempted and independently reviewed.
