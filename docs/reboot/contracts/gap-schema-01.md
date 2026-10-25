# Calendar study day 169 — GAP-SCHEMA-01: Pin the baseline rows and compatibility boundary

**Date:** 2027-04-12  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use disposable embedded PostgreSQL and migration V001 in the isolated schema-evolution lab. Record exact fixture IDs/values and create an independent expected row map for old and new readers.

## Source

[PostgreSQL 18 — ALTER TABLE](https://www.postgresql.org/docs/18/sql-altertable.html)

## Acceptance

Fixed expected rows and schema columns are asserted before any candidate migration.

## Command and evidence

`./mvnw -Dtest=SchemaEvolutionInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-01.json`. This is not learner completion until genuinely attempted and independently reviewed.
