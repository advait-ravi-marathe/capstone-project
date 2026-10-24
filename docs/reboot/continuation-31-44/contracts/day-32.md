# Day 32 — 2026-11-24: Idempotent policy issue

## Contract

**Learner-owned seam:** `PolicyLifecycleService.issue`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

A repeated clientRequestId for the same owner and request returns the original policy; the same owner/key with changed journey, quote or effective date throws the domain `IdempotencyConflictException` and leaves the original receipt unchanged. Day32 does not call an HTTP helper or later-day method.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-32.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day32IdenticalIdempotencyReplayReturnsOriginalAndChangedPayloadConflicts`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day32IdenticalIdempotencyReplayReturnsOriginalAndChangedPayloadConflicts test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Interview-first task proposal (5 minutes; inside the 85-minute practice block)

Before code, hints, worked answers, or AI, write: Write the owner/key/request-intent replay rule and one changed-payload conflict before opening the retry guide.

**Task boundary:** Use the existing Day 32 idempotency selector; preserve the original receipt on changed intent. Use the existing day's acceptance selector and preserve its full criteria. This short proposal does not replace acceptance or add time. Record the prediction and actual result in the existing evidence record.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-32-receipt-trace.md](../../continuation-31-44/worked/day-32-receipt-trace.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: none; use the bounded authored/official bridge. Optional reference-only lessons (outside today's 25-minute scheduled path): none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [AWS Builders’ Library: Making retries safe with idempotent APIs](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) — Retries and semantic equivalence; Same client request ID, different intent.

**Study allocation (exactly 25:00 / 1,500 seconds):** focused official source 6:00; worked example 15:00; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 55 / 20 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Repeat actual issue calls; full original equality; three independent changed-intent conflicts; original unchanged after rejection; scoped owner/key; counted construction not repeated.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-32.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Keep design reasoning in the learner-led proposal or review when assigned; no post-hoc architectural justification is required.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
