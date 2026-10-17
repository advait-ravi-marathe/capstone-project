# Day 06 · 2028-06-23 · CAP-637 / CSM14-06

**Board contract:** [CAP-637](https://dwait-to-adwait.youtrack.cloud/issue/CAP-637) — CSM14-06 · 23 Jun 2028 — Accept resolved refreshes with atomic revalidation (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-03, CSM14-04, CSM14-05

## Contract focus

Implement the bounded accept-refresh application seam. Reauthorize, load the expected current head and re-read authoritative source state through public module contracts. Recompute the proposed result from the base/local intent and explicit per-conflict decisions; compare the expected generation/fingerprint. Only a fully valid, explicitly resolved plan can append a new immutable checkpoint and its required audit/journey changes. Reuse the existing transaction and lock-order mechanism so catalog publication versus acceptance has a defined order.

## Synthetic fixture

Preview P20 is based on C-A/r1 and catalog generation 20 with a single resolved H2 conflict. In the positive case nothing changes. In the negative case catalog advances to generation 21 before acceptance. A third case throws the prepared persistence fault between staged revision and required journey update. Unresolved/forged resolution choices are separate input cases.

## Acceptance checklist

- [ ] The positive case appends one exact resolved checkpoint, preserves the old revision and records only safe source/operation references.
- [ ] Generation/head/fingerprint mismatch rejects without applying any resolution or changing the journey.
- [ ] An unresolved conflict or a chosen version outside the authoritative allowed options cannot be accepted merely because a client calls it resolved.
- [ ] Injected failure leaves domain/audit/journey state atomic; repeat uses the same durable operation identity.
- [ ] A controlled publication-versus-accept schedule demonstrates the declared serialization rule; a preflight read alone is not claimed to close the race.

## Architecture/evidence

Save a transaction/lock-order sketch plus the observed positive/stale/rollback row sets. Explain why the comparison refresh cannot imply acceptance of quote, policy, price or insurance terms.

## Scope and provenance

This is an explicit acceptance protocol for comparison metadata, with new branch reconciliation outcomes; it does not alter existing money/submit acceptance contracts.

Structured contract hash: `1e1cd18ddf4ea93ce02b37bee9b42417cbe74d8c9b635e53e65e57f01fef8110`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap637_staleGenerationAndUnresolvedChoiceCannotAppend test`
