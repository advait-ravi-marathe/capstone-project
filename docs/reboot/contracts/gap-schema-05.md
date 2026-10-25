# Calendar study day 173 — GAP-SCHEMA-05: Check old/new reader and writer compatibility

**Date:** 2027-04-16  
**Budget:** 25/130/10 + shared 45 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Execute fixed old-shape read/write SQL before and after backfill, and new-shape reads after it. Verify exact row sets and that the additive field does not break old code paths.

## Source

[PostgreSQL 18 — ALTER TABLE](https://www.postgresql.org/docs/18/sql-altertable.html)

## Acceptance

Actual SQL queries for both schema generations match independent expected rows.

## Command and evidence

`./mvnw -Dtest=SchemaEvolutionInfrastructureTest#oldAndNewReadersRemainCompatibleAcrossBackfill test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-05.json`. This is not learner completion until genuinely attempted and independently reviewed.
