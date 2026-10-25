# Calendar study day 175 — GAP-SCHEMA-07: Verify cleanup eligibility and publish migration evidence

**Date:** 2027-04-18  
**Budget:** 25/130/10 + shared 45 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

In a new disposable database, run the full expand/backfill/compatibility sequence and only contract after an independent retired-reader fixture says removal is safe. Capture exact migration history and row digest.

## Source

[PostgreSQL 18 — ALTER TABLE; Flyway](https://documentation.red-gate.com/flyway/database-development-using-flyway/working-with-data)

## Acceptance

Contract precondition is tested both true and false; exact expected rows, column set, and Flyway history are checked.

## Command and evidence

`./mvnw -Dtest=SchemaEvolutionInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-07.json`. This is not learner completion until genuinely attempted and independently reviewed.
