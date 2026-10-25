# Day 42 — 2026-12-06: Block post-cancellation writes

## Contract

**Learner-owned seam:** `PolicyLifecycleService.permitsServicingWrite`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

A cancelled policy rejects endorsements and rescheduling with 409; retries cannot add history; owner read remains available and shows the final state.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-42.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day42CancelledPolicyRejectsWritesButRemainsReadable`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day42CancelledPolicyRejectsWritesButRemainsReadable test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Interview-first system-design mock (45 minutes; inside the 85-minute practice block)

**Prompt:** [Q11 — Scheduler: run one job](../../interview/question-bank.md#q11). For this interview question only, save the initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the exact [100-point rubric](../../interview/mock-rubric.md) and its 5/5/15/10/10 phases.

**Existing EAI work (40 minutes):** Implement only the pure `permitsServicingWrite` cancelled-state guard and run the existing selector. The selector checks that predicate and reads a cancelled snapshot through an in-memory lambda; it does not issue an endorsement or reschedule, call an HTTP endpoint, prove 409, exercise a retry, inspect persisted history, or establish the Day 41 transition.

**Runnable acceptance for S3-D42-1 only:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day42CancelledPolicyRejectsWritesButRemainsReadable test`. This selector may remain red at an earlier learner-owned prerequisite; record the actual first blocker. It cannot close S3-D42-2, and it is not HTTP, write-path, retry, or database-history evidence.

**Deferred queue:** **S3-D42-1** — Complete the pure cancelled-state guard and the selector’s synthetic owner-read assertion. This selector proves neither real write refusal nor persistence.

**Deferred queue:** **S3-D42-2 (OPEN; prerequisite-gated)** — After the Day 38/39 endorsement write path and the persisted Day 41 cancellation transition exist, add runnable checks at the actual write boundary for both endorsement and rescheduling, repeat each rejected request, and compare complete history snapshots before and after. Assert HTTP 409 only if an actual mapped endpoint exists. No current Day 42 selector proves this work, and the existing selector cannot close this queue item. Day 42 acceptance remains pending until the original full criteria pass.

**Supporting source bridge:** Keep the existing “Java Tip 14 - Why Enum and Enum Basics - ordinal and values” (7:28) as status-vocabulary foundation only. Use the existing Java SE 21 Clock and AWS runbook documentation for due-time/recovery concepts; no additional lesson is assigned.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-42](<../../assistant-architecture-notes-15-70.md#day-42>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-42-operational-handover.md](../../continuation-31-44/worked/day-42-operational-handover.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Java Tip 14 - Why Enum and Enum Basics - ordinal and values (7:28). Optional reference-only lessons (outside today's 25-minute scheduled path): Java Tip 15 - Enum - Constructor, variables and methods. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [AWS Well-Architected Operational Excellence: Use runbooks to perform procedures](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/ops_ready_to_support_use_runbooks.html) — Implementation guidance; Implementation steps.
- [Java SE 21: Enum](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Enum.html) — Class Enum; name(); ordinal().

**Study allocation (exactly 25:00 / 1,500 seconds):** video or state fallback 7:28; focused runbook 3:00; worked example 10:32; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example, and review of the supplied decision share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 45 minutes for the named interview mock, then 40 minutes for this day’s bounded engineering slice: 5 minutes to confirm preconditions and state the first expected result, 25 minutes on the named learner seam, and 10 minutes for the named selector plus actual result/blocker evidence. This replaces the former full 85-minute implementation breakdown on this mock day only; the original acceptance remains required and unfinished work stays in the named deferred queue. Do not add time or mark the day complete from the mock or partial slice.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Acceptance focus:** Cancelled blocks writes while owned read remains available; executable short handover with a failure branch.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-42.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
