# Day 04 · 2028-06-21 · CAP-635 / CSM14-04

**Board contract:** [CAP-635](https://dwait-to-adwait.youtrack.cloud/issue/CAP-635) — CSM14-04 · 21 Jun 2028 — Fence concurrent branch saves and replay (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-03

## Contract focus

Implement the checkpoint-save command’s expected-revision check and reuse the existing durable idempotency envelope. Scope an operation to trusted owner, resource and operation kind; compare the canonical request content. Exercise two prepared PostgreSQL connections with barriers, not sleeps. One branch based on r1 may win; a competing changed branch must receive a conflict and retain its draft for explicit re-preview.

## Synthetic fixture

Tab T1 and T2 both start at C-A/r1. T1 proposes H1/v2; T2 proposes H2/v2. Both are released at a controlled write barrier. Retry T1 with the same operation key/content after its response is suppressed; then reuse that key with T2 content. A second resource has the same textual key but a distinct authorized operation scope.

## Acceptance checklist

- [ ] Exactly one concurrent changed branch advances the head from r1; the loser cannot overwrite the winning revision.
- [ ] The same accepted key/content returns the stored result even after response loss, without a second revision or durable domain effect.
- [ ] Changed content under the same scoped operation key conflicts and retains the original stored result.
- [ ] The losing response provides a safe stale-revision outcome, not an instruction to discard/reapply the draft automatically.
- [ ] The test records the real two-connection schedule and chosen linearization point; sequential mock calls are labeled insufficient concurrency evidence.

## Architecture/evidence

Draw at most eight schedule events and identify the database operation that linearizes acceptance. Compare optimistic expected-revision checks with a long-held application lock using the observed contention, not assumed distributed scale.

## Scope and provenance

The race protects a new branch head and immutable revisions; it is distinct from the original payment unknown-outcome and browser query-race exercises.

Structured contract hash: `1f435902724797136c792a02e8b2e6b467f97fb22007d2cd06995e50139c686c`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap635_twoConcurrentSavesHaveOneWinnerAndSameKeyReplay test`
