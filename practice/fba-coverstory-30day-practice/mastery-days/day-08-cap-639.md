# Day 08 · 2028-06-25 · CAP-639 / CSM14-08

**Board contract:** [CAP-639](https://dwait-to-adwait.youtrack.cloud/issue/CAP-639) — CSM14-08 · 25 Jun 2028 — Recover a lost refresh acceptance response (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `true`.

**Dependencies:** CSM14-04, CSM14-06, CSM14-07

## Contract focus

Wire and test the refresh panel’s retry/reconciliation path using the existing durable command result. The server commits a resolved checkpoint but the prepared transport drops the response. The UI must show an unknown outcome, query/replay the same authorized operation identity and reconcile with the stored current checkpoint. Add the smallest adapter/state seam required; do not implement a generic offline queue or persist private mutation payloads in browser storage.

## Synthetic fixture

C-A/r1→r2 acceptance commits; response is suppressed. The user clicks retry while the same page retains the original operation identity. Then run a full SPA reload: authenticate and refetch the existing owned shortlist/head; do not pretend that lost in-memory unsaved choices survived. Use a prepared stale-read response to check truthful loading until authoritative refetch completes.

## Acceptance checklist

- [ ] Response loss does not display definite failure or trigger a new logical acceptance command automatically.
- [ ] Same-operation retry returns r2 with exactly one durable revision/effect; explicit authoritative refetch settles the UI.
- [ ] The reloaded SPA reconstructs the persisted r2 from authenticated server reads and states honestly that any unsaved memory-only draft was not recovered.
- [ ] Stale or pending responses cannot downgrade r2 to r1 or falsely claim a new save.
- [ ] No private draft/mutation cache is written to localStorage, IndexedDB or a third-party endpoint.

## Architecture/evidence

Retain a bounded failure/recovery sequence separating commit, delivery and observation. Explain what survives page reload and why a transport timeout is not evidence of transaction rollback.

## Scope and provenance

This transfers the existing unknown-outcome principle into the newly built refresh UI/command protocol and proves its particular durable result identity.

Structured contract hash: `ea95d80387db25af8134c24b7ddb00456973da9592b66dab97c2da4cf522835f`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap639_lostResponseReplaysSameOperationAndRefetchesPersistedHead test`
Browser learner acceptance: `node web/scripts/csm14-learner-acceptance.mjs CAP-639` (asserts same identity and canonical request replay after unknown delivery, authoritative head refetch, and a negative definite-conflict case). It currently fails the behavior assertions at the preserved learner seam.
