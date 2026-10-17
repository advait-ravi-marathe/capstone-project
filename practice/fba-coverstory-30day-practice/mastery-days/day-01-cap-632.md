# Day 01 · 2028-06-18 · CAP-632 / CSM14-01

**Board contract:** [CAP-632](https://dwait-to-adwait.youtrack.cloud/issue/CAP-632) — CSM14-01 · 18 Jun 2028 — Model branch-safe comparison refresh invariants (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `true`.

**Dependencies:** CAP-601, CAP-54, CAP-166, CAP-202

## Contract focus

Add immutable Java input/result types and an executable invariant validator for refreshing an owned comparison checkpoint. A checkpoint pins 1–3 unique health product/source-version references, their source digests, scenario, stable slot IDs, display order and a checkpoint revision. A local branch carries its base revision and proposed selection; the Catalog public port supplies an upstream candidate. Model RESOLVED, CONFLICT and UNAVAILABLE as different outcomes. Implement the validator and its tests, not a complete merge algorithm.

## Synthetic fixture

SYNTHETIC owner A has existing owned shortlist S-A and journey J-A. Checkpoint C-A/r1 has slots s1→H1/v1, s2→H2/v1, s3→H3/v1 in that order, all scenario-health-001; B owns S-B/J-B. Catalog generation 20 includes H1/v2, H2/v1 and H3/v1. All source text is public synthetic wording; no consumer answers or free-text notes are present. An unavailable H3 lookup has status UNAVAILABLE, not an absent selected slot.

## Acceptance checklist

- [ ] The validator accepts the valid three-slot fixture and retains the exact base revision/source digests without mutating caller collections.
- [ ] Repeated product identity, four slots, incompatible scenario or missing source identity produces a typed invalid result with no partial checkpoint.
- [ ] A source read that is unavailable cannot be represented as an intentional deletion or an unchanged/Included result.
- [ ] A mutated input list cannot change an already-constructed immutable checkpoint; supply and run that adversarial test.

## Architecture/evidence

Record a maximum six-node ownership/data diagram and an executable invariant table. Explain why stable slot identity, source version and checkpoint revision are separate identities. State the maximum 3-slot assumption and a condition that would invalidate the chosen representation.

## Scope and provenance

The original curriculum already saves selections/resumes journeys. This new contract starts a distinct branch-versus-upstream refresh model; it does not rebuild save/resume or repeat the final filter/cursor exercise.

Structured contract hash: `3da50074fcda4533230602e9e820910d0645ff2feaf130c024cf07123271e33a`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap632_acceptsImmutableThreeSlotCheckpointAndRejectsDuplicateProducts test`
