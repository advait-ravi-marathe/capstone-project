# Day 10 · 2028-06-27 · CAP-641 / CSM14-10

**Board contract:** [CAP-641](https://dwait-to-adwait.youtrack.cloud/issue/CAP-641) — CSM14-10 · 27 Jun 2028 — Evolve checkpoint provenance without breaking readers (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-03, CSM14-07

## Contract focus

Add one backward-compatible optional provenance code to resolved checkpoint metadata using the supplied migration harness: LOCAL_UNCHANGED, UPSTREAM_REFRESH or EXPLICIT_CONFLICT_CHOICE. Historical rows are UNKNOWN/UNRECORDED, never guessed. Implement the additive PostgreSQL migration and typed API/React compatibility seam without changing immutable source references or requiring a new service. Use prepared copies of the day-7 consumer and current consumer to exercise old/new payloads.

## Synthetic fixture

Prepared r1 lacks provenance; r2 has an upstream refresh and an explicit conflict choice. One response adds the optional field to schemaMajor 1; one has an unsupported required schema major. The disposable database starts from the pinned pre-migration schema and includes the existing immutable row hashes.

## Acceptance checklist

- [ ] Migration preserves existing revision/source identity and data; old rows remain explicitly unrecorded rather than labeled as independently resolved.
- [ ] The prepared older compatible consumer tolerates the optional field under its actual contract; if it does not, record the incompatibility instead of claiming compatibility.
- [ ] The new consumer reads old payloads with Unknown provenance and rejects an unsupported required schema major safely.
- [ ] A migration failure leaves the disposable database in a known rolled-back or explicitly failed state; no destructive rollback of retained history is attempted.
- [ ] Record the actual old/new application-schema compatibility matrix and commands; this local test is not a deployment or rollback guarantee.

## Architecture/evidence

Retain the two-consumer/two-payload compatibility matrix and the expansion decision. Explain what a backward-compatible optional field can preserve and why it cannot manufacture historical evidence.

## Scope and provenance

This evolves a real artifact built in this sprint, rather than repeating a detached schema-expansion or lineage exercise.

Structured contract hash: `6c70d8d01d1640b6b93b6c7cd797e3a47981a32b01af0017ff048f25fe4f9d1a`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Migration acceptance (expected TODO until executable migration is written): `./mvnw -Plearner-tests -Dtest=CAP641CheckpointProvenanceAcceptanceTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CAP641CheckpointProvenanceAcceptanceTest test`
Browser learner check: `node web/scripts/csm14-learner-acceptance.mjs CAP-641` (expected TODO until historical/major-version decoding is implemented).
Browser learner check: `node web/scripts/csm14-learner-acceptance.mjs CAP-641`
