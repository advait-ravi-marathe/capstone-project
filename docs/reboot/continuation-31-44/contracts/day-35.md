# Day 35 — 2026-11-27: Atomic PostgreSQL issuance

## Contract

**Learner-owned seam:** `PolicyLifecycleService.persistIssueAtomically(dataSource, request, authenticatedOwnerId)`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

In one transaction on one connection, insert policy, issue event and idempotency receipt. On conflict roll back all new rows and publish no in-memory replay receipt. On success commit first, then publish the committed snapshot for same-service replay. Verify exact fresh-read rows and replay identity with disposable embedded PostgreSQL.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-35.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance commands:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt test` and `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35ReceiptConflictRollsBackPolicyAndEvent test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Interview-first system-design mock (45 minutes; inside the 85-minute practice block)

**Prompt:** [Q13 — Booking: reserve one seat atomically](../../interview/question-bank.md#q13). For this interview question only, save the initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the exact [100-point rubric](../../interview/mock-rubric.md) and its 5/5/15/10/10 phases.

**Existing EAI work (40 minutes):** 40 minutes: implement the successful one-connection policy + issue-event + owner/key receipt write and verify it through the fresh PostgreSQL read in the existing positive selector. Keep the whole write in the existing transaction boundary.

**Runnable acceptance:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt test`. This selector may remain red at an earlier learner-owned prerequisite; record the actual first blocker. Do not weaken it or claim the fixture is learner integration.

**Deferred queue:** **S3-D35-1** — Complete the injected receipt-conflict rollback proof, showing no policy/event/receipt partial write and no in-memory replay publication; rerun both existing Day 35 acceptance selectors. Day acceptance stays pending until the original full criteria pass.

**Supporting source bridge:** Conditional reference only: “Step 63 - Implementing Transaction Management - 3 Things to Decide” (4:38), item 7907620, access unconfirmed. Use the already assigned PostgreSQL transaction tutorial and ADR bridge if unavailable; do not enroll or add time.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-35](<../../assistant-architecture-notes-15-70.md#day-35>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-35-atomic-write-decision.md](../../continuation-31-44/worked/day-35-atomic-write-decision.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Step 63 - Implementing Transaction Management - 3 Things to Decide (4:38). Optional reference-only lessons (outside today's 25-minute scheduled path): Step 58 - Introduction to Transaction Management. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [AWS Prescriptive Guidance: Appendix – ADR template](https://docs.aws.amazon.com/prescriptive-guidance/latest/architectural-decision-records/appendix.html) — Context; Decision; Consequences; Compliance.
- [PostgreSQL 18 tutorial](https://www.postgresql.org/docs/18/tutorial-transactions.html) — 3.4. Transactions.

**Study allocation (exactly 25:00 / 1,500 seconds):** video or tx fallback 4:38; focused ADR 4:00; worked example 12:22; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example, and review of the supplied decision share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 45 minutes for the named interview mock, then 40 minutes for this day’s bounded engineering slice: 5 minutes to confirm preconditions and state the first expected result, 25 minutes on the named learner seam, and 10 minutes for the named selector plus actual result/blocker evidence. This replaces the former full 85-minute implementation breakdown on this mock day only; the original acceptance remains required and unfinished work stays in the named deferred queue. Do not add time or mark the day complete from the mock or partial slice.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Acceptance focus:** Fresh PostgreSQL reads of exact preserved old rows plus no new policy/event after receipt conflict; success all three rows.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-35.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
