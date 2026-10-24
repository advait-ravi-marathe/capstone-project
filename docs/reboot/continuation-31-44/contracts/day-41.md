# Day 41 — 2026-12-03: Apply due cancellation

## Contract

**Learner-owned seam:** `PolicyLifecycleService.applyDueCancellation`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

At injected Clock time equal to or after effective date, transition once to CANCELLED and append one system history event; before date remain scheduled.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-41.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day41CancellationRunsOnlyAtOrAfterEffectiveDateAndOnlyOnce`
**Persisted overload acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day41PersistedDueCancellationUpdatesPolicyAndEventAtomically`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance commands:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day41CancellationRunsOnlyAtOrAfterEffectiveDateAndOnlyOnce test` and `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day41PersistedDueCancellationUpdatesPolicyAndEventAtomically test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.

**Task boundary:** Run both existing pure and persisted Day 41 selectors; do not infer atomic persistence from the pure transition. Use the existing day's acceptance selector and preserve its full criteria.
## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-41](<../../assistant-architecture-notes-15-70.md#day-41>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-41-clock-transition.md](../../continuation-31-44/worked/day-41-clock-transition.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: none; use the bounded authored/official bridge. Optional reference-only lessons (outside today's 25-minute scheduled path): none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [Java SE 21: Clock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) — Class Clock; fixed(Instant, ZoneId).

**Study allocation (exactly 25:00 / 1,500 seconds):** focused clock 4:00; worked example 17:00; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example, and review of the supplied decision share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 55 / 20 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Acceptance focus:** Before/equal/after injected-clock checks; repeated final transition unchanged; integrated state/event atomicity not inferred from pure test.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-41.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
