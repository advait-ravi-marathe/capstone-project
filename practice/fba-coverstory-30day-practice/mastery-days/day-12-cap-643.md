# Day 12 · 2028-06-29 · CAP-643 / CSM14-12

**Board contract:** [CAP-643](https://dwait-to-adwait.youtrack.cloud/issue/CAP-643) — CSM14-12 · 29 Jun 2028 — Implement a differential branch-history checker (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-04, CSM14-06, CSM14-09

## Contract focus

Author a small executable reference state model independently of the production rebase implementation, then replay bounded command schedules through both model and actual application service. Compare immutable history/head, operation outcomes and source identities. Use state-table rules, not production helper methods, as the oracle. Add one regression for any genuine difference; do not manufacture a defect or silently change the oracle to match the candidate.

## Synthetic fixture

Use two local branches, two catalog generations and at most six commands per trace. The instructor supplies 12 finite seeded schedules combining expected-revision conflict, same-key retry, changed-key payload conflict and rollback; the learner authors four additional fresh schedules before inspecting production results. All reset/seed mechanics are already prepared.

## Acceptance checklist

- [ ] The independent reference model predicts each trace before the production run and declares any simplifying assumptions.
- [ ] Every accepted operation creates at most one durable revision/effect; rejected/conflicting/rolled-back operations preserve the expected old head and required atomic records.
- [ ] A deliberately defective disposable candidate fails at least one checker assertion for the intended semantic reason; the normal candidate result is recorded honestly.
- [ ] The checker reports the earliest divergent command and exact state difference without dumping private payloads.
- [ ] Guidance, shared implementation logic or disclosed fault mechanisms are recorded; they cannot be mislabeled independent model design.

## Architecture/evidence

Explain the model’s abstraction boundary, the independent oracle’s weaknesses and what its bounded traces cannot prove. Include one counterexample that would pass a simple final-row-count assertion but fail this history checker.

## Scope and provenance

This new differential checker analyzes the complete branch acceptance protocol, rather than redoing a portfolio evidence checker or a general debugging interview.

Structured contract hash: `86c2272af4bdb1f5d4acf216db662dfabe8fcd4fa9ba86c4fefdac95084cbf20`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap643_independentReplayModelPredictsBoundedCommandTrace test`
