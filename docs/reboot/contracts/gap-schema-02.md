# Calendar study day 170 — GAP-SCHEMA-02: Apply an additive expand migration

**Date:** 2027-04-13  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Apply the lab's versioned nullable column migration. Verify the preexisting rows remain exact, old insert/read SQL still works, and the new column accepts a value.

## Source

[PostgreSQL 18 — ALTER TABLE](https://www.postgresql.org/docs/18/sql-altertable.html)

## Acceptance

Independent row snapshot is byte-stable across expansion and an old-shape insert remains valid.

## Command and evidence

`./mvnw -Dtest=SchemaEvolutionInfrastructureTest#expandMigrationPreservesRowsAndOldWriterCompatibility test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-02.json`. This is not learner completion until genuinely attempted and independently reviewed.
