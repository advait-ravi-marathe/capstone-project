# Calendar study day 172 — GAP-SCHEMA-04: Interrupt and resume without skips or duplicates

**Date:** 2027-04-15  
**Budget:** 25/130/10 + shared 45 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use fresh bounded-runner invocations against only the persisted database cursor; this is not a claimed JVM-crash reproduction. Inject a failure after the first row update and before checkpoint commit, verify transaction rollback, then resume and compare all ordered rows/cursor exactly.

## Source

[PostgreSQL 18 — UPDATE](https://www.postgresql.org/docs/18/sql-update.html)

## Acceptance

Failure injection proves atomic rollback before resume; exact IDs, full values, version/update counts, final cursor and wrong-cursor mutant are checked.

## Command and evidence

`./mvnw -Dtest=SchemaEvolutionInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-04.json`. This is not learner completion until genuinely attempted and independently reviewed.
