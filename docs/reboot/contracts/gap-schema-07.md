# Calendar study day 175 — GAP-SCHEMA-07: Verify cleanup eligibility and publish migration evidence

**Date:** 2027-04-18  
**Budget:** 25/130/10 + shared 45 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL. No capstone design proposal or post-hoc design defense is assigned.

## Task

In a new disposable database, run the learner bounded backfill, prove exact values/cursor/update counts, and contract only when every old reader and writer is retired and backfill completion is verified. Capture exact migration history and row digest.

## Source

[Flyway — Working with data](https://documentation.red-gate.com/flyway/database-development-using-flyway/working-with-data)

## Acceptance

Learner backfill/gate acceptance plus independent PostgreSQL fixture verify complete exact rows, both reader/writer retirements, migration history, and denied premature contract.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=ResumableBackfillAcceptanceTest,SchemaContractGateAcceptanceTest test && ./mvnw -Dtest=SchemaEvolutionInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-schema-07.json`. This is not learner completion until genuinely attempted and independently reviewed.
