# Day 40 — 2026-12-02: Schedule cancellation

## Contract

**Learner-owned seam:** `PolicyLifecycleService.scheduleCancellation(policy, request, PolicyCancellationReceiptStore)`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

A seeded `PolicyCancellationReceiptStore` drives the replay cases. Same key/date/reason returns the stored original; the same key with a changed date throws `IdempotencyConflictException`; a date equal to the injected current date is accepted as non-past, while a past date throws `IllegalArgumentException`. Unknown reasons and mismatched expected versions are rejected. The historic selector name “Future” is retained for command compatibility; it does not define a strict-future rule.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-40.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day40CancellationMustBeFutureAndRequestReplayIsIdempotent`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day40CancellationMustBeFutureAndRequestReplayIsIdempotent test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-40-cancellation-intent.md](../../continuation-31-44/worked/day-40-cancellation-intent.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Step 13 - Java Dates - Exploring LocalDate - Comparing Dates and Creating Specif (5:20). Optional reference-only lessons (outside today's 25-minute scheduled path): none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [Java SE 21: LocalDate](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html) — now(Clock); isBefore(ChronoLocalDate); isAfter(ChronoLocalDate).
- [AWS Builders’ Library: Making retries safe with idempotent APIs](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) — Retries and semantic equivalence; Same client request ID, different intent.

**Study allocation (exactly 25:00 / 1,500 seconds):** video or date fallback 5:20; worked example 15:40; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 60 / 15 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Stored original replay despite old expectedVersion; changed date/reason conflict; independent version/reason/date negatives; same-day non-past semantics preserved.

## Evidence allocation (10 minutes)

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
