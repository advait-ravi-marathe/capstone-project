# Day 43 — 2026-12-05: React servicing summary

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

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-43-response-state-ui.md](../../continuation-31-44/worked/day-43-response-state-ui.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Rendering Content Conditionally (8:03). Replacement-only optional lessons: none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [React: Conditional Rendering](https://react.dev/learn/conditional-rendering#conditionally-returning-jsx) — Conditionally returning JSX.
- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure#avoid-contradictions-in-state) — Avoid contradictions in state.
- [Testing Library: ByRole](https://testing-library.com/docs/queries/byrole/) — API.

**Study allocation (exactly 25:00 / 1,500 seconds):** video or React fallback 8:03; focused source and prerequisite 3:57; worked component trace test 9:00; transfer prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 40 / 25 / 10 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Full allowlisted backend projection and React empty/stale/error/recovery/variant values; no component-test customer-UAT claim.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-43.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Keep design reasoning in the learner-led proposal or review when assigned; no post-hoc architectural justification is required.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
