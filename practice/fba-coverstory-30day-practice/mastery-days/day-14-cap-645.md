# Day 14 · 2028-07-01 · CAP-645 / CSM14-14

**Board contract:** [CAP-645](https://dwait-to-adwait.youtrack.cloud/issue/CAP-645) — CSM14-14 · 01 Jul 2028 — Build an executable rebase admission gate (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-08, CSM14-09, CSM14-11, CSM14-12, CSM14-13

## Contract focus

Implement one bounded end-to-end contract test/admission runner against this sprint’s actual Java/Spring/React/PostgreSQL candidate. The instructor supplies one fresh six-event schedule that combines an unsaved reordered branch, a source update and either a concurrent accepted branch or a lost acceptance response. The learner states the predicted exact visible, API and durable outcomes before running it, authors the oracle and executes the test. Keep the runner scoped to this new feature; do not repeat a portfolio presentation or all-course review.

## Synthetic fixture

Default schedule: load A/C-A/r1; locally reorder s3,s1,s2; preview at catalog generation 20; another prepared tab accepts r2; submit the original resolved preview; explicitly obtain a new preview and decide whether to accept. The old preview must conflict without changing r2. Before assignment the instructor uses a fresh hidden interleaving/identifiers from the declared supported domain; its solution and failing boundary are not supplied. The environment also contains a prepared defective variant for oracle self-check.

## Acceptance checklist

- [ ] The learner-written test observes UI, API outcome and independent PostgreSQL state for the same candidate and schedule.
- [ ] A stale old branch cannot overwrite the concurrently accepted revision; explicit refresh/review retains the user’s local intent or clearly identifies remaining conflicts.
- [ ] Every accepted result matches exact authoritative version refs, owner and immutable history; no money/policy/quote acceptance changes.
- [ ] The prepared defective variant fails for the intended contract violation, and the candidate result is retained as PASS, FAIL or BLOCKED with raw evidence.
- [ ] The final design defense cites this executed schedule plus the sprint’s actual transaction, compatibility and bounded-query evidence; no unexecuted completion, production-scale, security or independent-mastery claim is added.

## Architecture/evidence

Deliver a compact candidate-bound system design: module/public-port map, one acceptance sequence, transaction/authorization boundaries, observed query budget and one explicit scaling or failure assumption. Defend one alternative from measured evidence and name the next unresolved limit. The artifact is backed by the code/test authored today.

## Scope and provenance

This is a new executable cross-layer admission gate for branch-safe rebase, not the original final evidence-index/demo day.

Structured contract hash: `05a7267773892a7f2804cae8287f7a0075b45c7cac35999ef5ac466c5668ae34`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap645_admissionRunnerChecksUiApiAndIndependentPostgres test`
Admission runner entry point: `experiments/coverstory/checkpoint-refresh/run-admission-contract.sh` (expected TODO failure until implemented).
