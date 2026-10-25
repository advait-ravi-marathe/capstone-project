# Calendar study day 174 — GAP-SCHEMA-06: Reject destructive change and demonstrate forward recovery

**Date:** 2027-04-17  
**Budget:** 25/130/10 + shared 45 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

Prove a destructive candidate migration fails the schema/row acceptance boundary. Recover only by applying a forward migration in a fresh disposable DB and compare exact rows.

## Source

[PostgreSQL 18 — ALTER TABLE](https://www.postgresql.org/docs/18/sql-altertable.html)

## Acceptance

A dropped-column mutant is rejected; forward migration restores the expected schema/values without claiming rollback semantics.

## Command and evidence

`./mvnw -Dtest=SchemaEvolutionInfrastructureTest#forwardMigrationRepairsDriftAndContractRequiresRetiredReaders test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-06.json`. This is not learner completion until genuinely attempted and independently reviewed.
