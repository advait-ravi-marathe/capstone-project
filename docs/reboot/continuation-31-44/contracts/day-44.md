# Day 44 — 2026-12-06: Lifecycle integration acceptance contract

## Contract

**Learner-owned seam:** `PolicyLifecycleService.verifyLifecycleReadback`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

The completed learner slice must prove issue, replay, endorsement, history, scheduled cancellation and readback through the React browser, Spring API and disposable PostgreSQL, including persisted servicing summary fields, a controlled summary-read failure/recovery, and restart persistence. A tagged learner acceptance drives the actual panel against an owner-scoped JDBC read and real summary endpoint in the test harness; it remains red at learner TODOs. A separate expected-vs-actual UAT record is generated only from observed runs. A separate instructor transport smoke writes and reads a synthetic harness row through the browser and Spring API, restarts the Spring server while keeping PostgreSQL alive, then confirms persisted readback. That smoke validates the reproducible runtime harness only; it does not claim lifecycle behavior is implemented.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-44.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day44LifecycleIntegrationSeamMustVerifyReadback`
**Infrastructure selectors:** `assignments.reboot.Day44BrowserTransportInfrastructureTest#realBrowserRoundTripSurvivesSpringServerRestart` plus `assignments.reboot.ContinuationManifestInfrastructureTest`
**Extended learner selector:** `Day44BrowserTransportInfrastructureTest#realBrowserObservesServicingFailureRecoveryAndRestart`
**Corrupt-readback selector:** `PolicyLifecycleAcceptanceTest#day44ReadbackRejectsMissingPolicyIncompleteHistoryAndStatusVersionMismatch`

**Learner acceptance commands:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day44LifecycleIntegrationSeamMustVerifyReadback test`, `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day44ReadbackRejectsMissingPolicyIncompleteHistoryAndStatusVersionMismatch test`, `./mvnw -Plearner-tests -Dtest=Day44BrowserTransportInfrastructureTest#realBrowserDrivesPolicyLifecycleApiThroughSpringAndPostgres test` and `./mvnw -Plearner-tests -Dtest=Day44BrowserTransportInfrastructureTest#realBrowserObservesServicingFailureRecoveryAndRestart test`.
**Instructor infrastructure commands:** `npm --prefix frontend run build` then `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Dtest=Day44BrowserTransportInfrastructureTest#realBrowserRoundTripSurvivesSpringServerRestart,ContinuationManifestInfrastructureTest test`. CI installs Chromium through Playwright and uses its managed executable.

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.
## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

**Practice allocation (85 minutes):** 5 minutes for the expected-versus-observed proposal, then the remaining 80 minutes for the original learner acceptance and its existing prerequisites. The instructor transport smoke remains infrastructure evidence only. If a prerequisite is unresolved or time ends, leave the full learner acceptance open and record the blocker.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-44](<../../assistant-architecture-notes-15-70.md#day-44>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-44-observed-uat-handover.md](../../continuation-31-44/worked/day-44-observed-uat-handover.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: none; use the bounded authored/official bridge. Replacement-only optional lessons: none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [ISTQB Certified Tester Acceptance Testing syllabus v1.0 2019](https://istqb.org/wp-content/uploads/2024/11/ISTQB-CT-AcT_Syllabus_v1.0_2019.pdf) — 5.2.1 Defect Analysis; 5.2.2 Reporting.
- [Spring Boot: Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) — MOCK and RANDOM_PORT behavior in opening section.
- [Chrome Headless mode](https://developer.chrome.com/docs/automation-and-testing/headless#use-headless-mode) — Use Headless mode.
- [Playwright: Network](https://playwright.dev/docs/network#network-mocking) — Network mocking.

**Study allocation (exactly 25:00 / 1,500 seconds):** focused UAT sections 4:00; worked observation and harness trace 17:00; prerequisite gate 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example, and review of the supplied decision share this block. Viewing is not learner completion.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Acceptance focus:** Actual learner panel against persisted state; aborted read/recovery; fresh Spring/browser restart same DB; no-op verifier killed by missing/corrupt-state tests; observed != expected.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-44.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
