# Day 03 · 2028-06-20 · CAP-634 / CSM14-03

**Board contract:** [CAP-634](https://dwait-to-adwait.youtrack.cloud/issue/CAP-634) — CSM14-03 · 20 Jun 2028 — Persist immutable checkpoint revisions atomically (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-01, CSM14-02, CAP-57, CAP-202

## Contract focus

Implement the prepared Shortlist persistence adapter for appending an immutable checkpoint revision and advancing its head using the existing unit-of-work/public module contracts. Store only owner-scoped references and permitted metadata, not copied private answers. The domain revision, required safe audit and existing relevant journey projection/step change must commit together. Reuse the existing encryption adapter wherever the established data policy requires it; do not create keys or a new payload system.

## Synthetic fixture

Prepared PostgreSQL schema has C-A/r1 and matching owned shortlist/journey. Append r2 referencing r1 with unchanged scenario and approved exact version identities. A disposable fault fires after staging the revision but before the required audit/journey writes. A separate read after rollback inspects durable state.

## Acceptance checklist

- [ ] A successful append preserves r1 byte-for-byte, persists r2 and advances only the appropriate owned head/projection.
- [ ] The injected failure leaves no r2, no advanced head and no orphan required audit/journey record.
- [ ] The adapter reaches other modules only through their public contracts; no cross-module repository shortcut is introduced.
- [ ] A duplicate immutable revision identity with changed content fails rather than overwriting history.
- [ ] Record the actual transaction boundary and database assertions; fixture-only in-memory results do not prove SQL durability.

## Architecture/evidence

Annotate the actual commit boundary and affected rows for success versus injected rollback. Explain the head-pointer/history trade-off and why an audit row rolled back in that transaction is not durable evidence of failure.

The tagged contract checks successful r1→r2 append, immutable r1 retention, exactly one required-effect append, stale/foreign-owner rejection, and injected failure rollback leaving only r1 with no effect. It also calls the disposable-PostgreSQL rollback witness against the prepared schema; this does not establish a production persistence adapter.

## Scope and provenance

This adds a new immutable comparison-checkpoint aggregate using existing transaction foundations, not another generic SQL history mock.

Structured contract hash: `f39abcd97e2bc8a1f1004db38a6182997fa97ea26b70d1ad8b950515f4455158`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap634_appendsHistoryAndRequiredEffectsAtomically test`
