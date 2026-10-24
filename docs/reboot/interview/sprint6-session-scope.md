# Sprint 6 interview sessions and engineering work

Sprint 6 covers Days 71–84 (2–15 January 2027). Each day retains 120 engineering minutes: 25 study, 85 practice/testing, and 10 evidence. The separate DSA allocation is outside those 120 minutes. No dates, learner status, or course-watched values are changed by this plan.

Four 45-minute system-design interviews are scheduled, two each week: Days 73 and 76, then Days 80 and 81. They use the exact Requirements (5), Capacity and constraints (5), Initial architecture and data model (15), Bottlenecks/reliability/scaling (10), and Trade-offs/failures/follow-ups (10) phases, with the 100-point rubric. The learner proposes a design only for the assigned question. For capstone implementation, the assistant supplies the concrete architecture and concise rationale before the learner implements and tests; no capstone proposal or post-hoc justification is assigned.

On mock days, the 45-minute interview replaces part of the existing practice block and leaves a bounded 40-minute EAI slice (5 prerequisite/prediction, 25 focused implementation or observation, 10 selector/result/blocker). It does not reduce or replace the full named acceptance gate. Unfinished assertions stay in the day's explicit deferred-work ID. Non-mock days keep the full 85-minute engineering practice allocation.

## Daily acceptance map

| Day | Work within the daily budget | Runnable acceptance |
|---:|---|---|
| 71 | Establish the synchronous Hikari/route baseline and prerequisite status; instructor evidence review, no feature. | `./mvnw -Dtest=Day71BaselineAcceptanceTest test`; `./mvnw -Dtest=Day71To84InfrastructureTest test` |
| 72 | Implement finite capacity validation and startup enforcement; verify positive bounds and edge values. | `./mvnw -Plearner-tests -Dtest=PreviewCapacityPolicyAcceptanceTest test` |
| 73 · mock | Q04, rate limiter fixed-window behavior. In the 40-minute EAI slice, implement/test two permits, immediate third-call rejection, and distinct lease results; release is Day 74.
 | `./mvnw -Plearner-tests -Dtest=PreviewAdmissionAcceptanceTest test` |
| 74 | Make lease close atomic and idempotent under repeated and concurrent calls. | `./mvnw -Plearner-tests -Dtest=PreviewLeaseAcceptanceTest test` |
| 75 | Decorate the existing owner-scoped read; preserve arguments, delegate once, release every path, translate only connection acquisition failure. | `./mvnw -Plearner-tests -Dtest=BoundedPolicyRenewalReadStoreAcceptanceTest test` |
| 76 · mock | Q35, distributed rate limiting. In the 40-minute EAI slice, run/inspect deterministic latch admission, failure recovery, and the second admission cycle. | `./mvnw -Plearner-tests -Dtest=PreviewAdmissionConcurrencyAcceptanceTest test` |
| 77 | Use bounded doubles to verify saturation rejects before delegation and released capacity serves a later read. | `./mvnw -Plearner-tests -Dtest=PreviewRecoveryAcceptanceTest test` |
| 78 | Map busy/unavailable to fixed sanitized 503 responses; preserve unrelated errors. | `./mvnw -Plearner-tests -Dtest=PreviewCapacityHttpAcceptanceTest test` |
| 79 | Wire exactly one singleton decorator around the actual existing store and fail startup on invalid settings. | `./mvnw -Plearner-tests -Dtest=PreviewRuntimeWiringAcceptanceTest test` |
| 80 · mock | Q33, metrics from events. In the 40-minute EAI slice, run the fixed-enum timer/counter acceptance and inspect only permitted tag values. | `./mvnw -Plearner-tests -Dtest=PreviewMetricsAcceptanceTest test` |
| 81 · mock | Q08, safe error contract. In the 40-minute EAI slice, implement/verify accessible busy/unavailable state and explicit user-triggered retry. | `cd frontend && RUN_LEARNER_ACCEPTANCE=true npx vitest run src/components/RenewalPreviewBusyState.test.tsx` |
| 82 | Hold all four test-owned pool connections, observe finite acquisition failure, release in `finally`, and prove the next useful read succeeds. | `./mvnw -Plearner-tests -Dtest=PreviewPoolRecoveryAcceptanceTest test` |
| 83 | Build fresh local assets and JAR; exercise the real Chromium/PostgreSQL lock harness, reject the third reader before JDBC, then recover after lock release and manual retry. | `npm --prefix frontend ci && scripts/package-sprint6-demo.sh && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests -Dtest=Day83PackagedLockBrowserAcceptanceTest test` |
| 84 | Review actual selector outcomes and independent learner/infrastructure/integration status; no new feature. | `./mvnw -Dtest=Day84ContractInfrastructureTest test`; `./mvnw -Dtest=Day71To84InfrastructureTest test` |

## Architecture, course and evidence boundaries

Days 79 and 83 use a dedicated `sprint6-local` runtime assembled from the two-argument owner-scoped adapter. It excludes the later read-budget policy and timed-store wrapper. The assistant's per-day architecture notes are at [`assistant-architecture-notes.md`](../continuation-71-84/assistant-architecture-notes.md) and linked from each contract and export. They match the actual learner seams and selector boundaries; they do not implement TODOs or imply integration success. Exact optional course title/lesson metadata remains in the manifest and contracts with `videos_watched: false`; access-unconfirmed items are not prerequisites and no purchase or enrollment is assigned. Official primary documentation supports Hikari, Java concurrency, Spring beans, HTTP status, Micrometer, ARIA and PostgreSQL locking. All study materials share the same 25-minute cap.

The canonical assignments are in [`manifest.json`](../continuation-71-84/manifest.json) and the owner Stories in [`scrum-owner-manifest.json`](../continuation-71-84/scrum-owner-manifest.json). DSA remains solely in the external `dsa-prep` repository; this sprint adds no DSA files or tracker.
