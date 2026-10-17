# Day 02 · 2028-06-19 · CAP-633 / CSM14-02

**Board contract:** [CAP-633](https://dwait-to-adwait.youtrack.cloud/issue/CAP-633) — CSM14-02 · 19 Jun 2028 — Implement deterministic three-way version reconciliation (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-01, CAP-251

## Contract focus

Implement a pure Java three-way reconciliation seam over base B, local branch L and upstream candidate U. For each stable slot, L=B permits U; U=B permits L; L=U permits that shared result; divergent L and U require an explicit conflict. Intentional removal is a typed local operation. Source UNAVAILABLE stays a separate blocking state. Preserve declared slot order and validate the final 1–3 unique same-scenario products. Do not fetch data, persist or resolve conflicts on the learner’s behalf.

## Synthetic fixture

For s1: B=H1/v1, L=H1/v1, U=H1/v2 gives an unambiguous refresh. For s2: B=H2/v1, L=H2/v2, U=H2/v3 gives conflict. For s3: local explicitly removes while U changes H3/v1→H3/v2, giving a remove-versus-change conflict. A separate no-change fixture has L=B=U. UNAVAILABLE is supplied independently of any missing map entry.

## Acceptance checklist

- [ ] Unambiguous cases follow the declared truth table and preserve exact source/version references.
- [ ] Both divergent-version and remove-versus-change cases expose unresolved conflict; no version wins by timestamp or map iteration order.
- [ ] No-change reconciliation is idempotent and the input objects remain unchanged.
- [ ] Reversing fixture map insertion order does not change conflict identities or declared result order; unavailable source evidence never becomes deletion.
- [ ] Execute at least one independently authored counterexample to the proposed equality/identity rule.

## Architecture/evidence

Retain the executable truth table, a measured operation count for 1 and 3 slots, and one rejected last-write-wins counterexample. Explain why constant small bounds do not eliminate semantic conflict and identify which part could generalize to O(n).

## Scope and provenance

This implements three-way selection semantics, rather than the old two-version clause diff, stable cursor or latest-per-family query.

Structured contract hash: `92ef20b820ed3add09a7661bd9e643f8b452061977fbdbe9db0be15bc0039821`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap633_reconcilesByThreeWayEqualityAndExposesDivergence test`
