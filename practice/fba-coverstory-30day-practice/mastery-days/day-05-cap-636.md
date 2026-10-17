# Day 05 · 2028-06-22 · CAP-636 / CSM14-05

**Board contract:** [CAP-636](https://dwait-to-adwait.youtrack.cloud/issue/CAP-636) — CSM14-05 · 22 Jun 2028 — Expose an authoritative refresh preview API (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-02, CSM14-04, CAP-259

## Contract focus

Implement the existing Spring API adapter for a proposed POST /api/v1/shortlists/{id}/checkpoint-refresh-previews. Authorize the existing shortlist before reading its private branch metadata. Resolve at most nine exact base/local/upstream version references across three selected product slots through one public Catalog snapshot port; produce exact checkpoint/catalog generations, source digests, conflict states and a canonical preview fingerprint. The server computes this fingerprint; it is an equality aid, never authorization. A preview does not accept a branch or advance journey state.

## Synthetic fixture

A previews C-A/r1 and the three-slot fixture at catalog generation 20. Add one divergent local edit, one optional response metadata field, an unavailable public source and a body owner override. Foreign B requests S-A. The Catalog adapter can expose a prepared generation-change signal during snapshot capture.

## Acceptance checklist

- [ ] The response identifies a single declared checkpoint revision and catalog snapshot, with exact conflicts and qualified unavailable sources.
- [ ] Foreign/unknown owned shortlist references use the established safe denial contract before private serialization; body owner overrides are rejected.
- [ ] Preview execution leaves checkpoint head, audit/domain transition count and journey state unchanged.
- [ ] A mixed catalog snapshot is rejected/retried only within the declared bounded adapter behavior; it is not presented as a coherent preview.
- [ ] Additive optional metadata cannot alter required source identities or silently resolve a conflict.

## Architecture/evidence

Record one request/response sequence showing owner authorization, Catalog public-port access and fingerprint construction. Explain the consistency point and why a client-supplied digest or UUID is not an access credential. Bound input fan-out to three slots and at most nine exact version references; a batch boundary does not authorize any private cross-owner read.

## Scope and provenance

The new API mediates a complete branch/base/upstream preview; it does not repeat the final explorer filter endpoint.

Structured contract hash: `f29d3b59761e852e9581cc694a9d808024347d614020566b41deed8061b31ad4`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap636_previewAuthorizesBeforeReadsAndNeverMutates test`
