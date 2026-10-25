# Day 43 — 2026-12-07: React servicing summary

## Contract

**Learner-owned seam:** `PolicyLifecycleService.toServicingView` and `PolicyServicingPanel`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

Render owner-scoped policy status, snapshot premium/version, endorsement history and cancellation date; loading, empty, 401 and 404 states are explicit; no sensitive fields.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-43.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `frontend/src/__tests__/Day43PolicyServicingPanel.test.tsx`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance commands:** `npm --prefix frontend run test:learner -- src/__tests__/Day43PolicyServicingPanel.test.tsx` and `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day43ServicingProjectionOmitsOwnerJourneyAndQuoteIds test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.
## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-43](<../../assistant-architecture-notes-15-70.md#day-43>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-43-response-state-ui.md](../../continuation-31-44/worked/day-43-response-state-ui.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Rendering Content Conditionally (8:03). Replacement-only optional lessons: none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [React: Conditional Rendering](https://react.dev/learn/conditional-rendering#conditionally-returning-jsx) — Conditionally returning JSX.
- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure#avoid-contradictions-in-state) — Avoid contradictions in state.
- [Testing Library: ByRole](https://testing-library.com/docs/queries/byrole/) — API.

**Study allocation (exactly 25:00 / 1,500 seconds):** video or React fallback 8:03; focused source and prerequisite 3:57; worked component trace test 9:00; transfer prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example, and review of the supplied decision share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 5 minutes for the task-first proposal, followed by up to 80 minutes for the original learner acceptance, respecting its existing prerequisite gates and scope. Instructor fixture setup is supplied and is not hidden learner implementation. If a prerequisite is unmet or the timebox ends, record the first blocker and keep unfinished acceptance open.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Acceptance focus:** Full allowlisted backend projection and React empty/stale/error/recovery/variant values; no component-test customer-UAT claim.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-43.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
