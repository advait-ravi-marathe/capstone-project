# Calendar study day 171 — GAP-SCHEMA-03: Backfill bounded batches with a durable cursor

**Date:** 2027-04-14  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Implement the resumable backfill learner seam over stable integer IDs using bounded ordered batches and a stored cursor. The expected target values and IDs are fixed independently.

## Source

[PostgreSQL 18 — UPDATE](https://www.postgresql.org/docs/18/sql-update.html)

## Acceptance

Acceptance compares exact IDs, values, cursor and total row count after each bounded batch.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=ResumableBackfillAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-03.json`. This is not learner completion until genuinely attempted and independently reviewed.
