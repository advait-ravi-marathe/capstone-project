# Day 37 — 2026-11-29: Endorsement command validation

## Contract

**Learner-owned seam:** `PolicyEndorsementRequest`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

Validate expected policy version, effective date, and allowlisted training change fields; reject unknown fields, no-op changes, past dates, and expected versions 0 or 2 when current version is 1.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-37.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day37EndorsementRejectsUnknownNoopPastDateAndStaleVersion`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day37EndorsementRejectsUnknownNoopPastDateAndStaleVersion test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Interview-first task proposal (5 minutes; inside the 85-minute practice block)

Before code, hints, worked answers, or AI, write: Name the accepted endorsement fields and one no-op, past-date, or stale-version rejection before opening validation guidance.

**Task boundary:** Use the existing Day 37 selector; invalid commands must not mutate policy or history. Use the existing day's acceptance selector and preserve its full criteria. This short proposal does not replace acceptance or add time. Record the prediction and actual result in the existing evidence record.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-37-independent-acceptance.md](../../continuation-31-44/worked/day-37-independent-acceptance.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Step 14 - Implementing Validations for REST API (11:47). Optional reference-only lessons (outside today's 25-minute scheduled path): none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [Spring Framework: Method Arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html) — java.security.Principal table entry; @RequestBody table entry.

**Study allocation (exactly 25:00 / 1,500 seconds):** verified video 11:47; worked example 9:13; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 55 / 20 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Independent reason/date/version/no-op cases with otherwise valid baseline.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-37.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Keep design reasoning in the learner-led proposal or review when assigned; no post-hoc architectural justification is required.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
