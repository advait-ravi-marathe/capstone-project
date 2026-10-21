# Day 44 — 2026-12-02: Lifecycle integration acceptance contract

## Contract

**Learner-owned seam:** `PolicyLifecycleService.verifyLifecycleReadback`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes total depending on 1–2/3/4 substantive DSA exercises + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). The evidence and teach-back fit inside these allocations; no setup task is added.


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

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-44-observed-uat-handover.md](../../continuation-31-44/worked/day-44-observed-uat-handover.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: none; use the bounded authored/official bridge. Replacement-only optional lessons: none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [ISTQB Certified Tester Acceptance Testing syllabus v1.0 2019](https://istqb.org/wp-content/uploads/2024/11/ISTQB-CT-AcT_Syllabus_v1.0_2019.pdf) — 5.2.1 Defect Analysis; 5.2.2 Reporting.
- [Spring Boot: Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) — MOCK and RANDOM_PORT behavior in opening section.
- [Chrome Headless mode](https://developer.chrome.com/docs/automation-and-testing/headless#use-headless-mode) — Use Headless mode.
- [Playwright: Network](https://playwright.dev/docs/network#network-mocking) — Network mocking.

**Study allocation (exactly 25:00 / 1,500 seconds):** focused UAT sections 4:00; worked observation and harness trace 17:00; prerequisite gate 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 35 / 25 / 15 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Actual learner panel against persisted state; aborted read/recovery; fresh Spring/browser restart same DB; no-op verifier killed by missing/corrupt-state tests; observed != expected.

## Evidence allocation (10 minutes)

Record command and result in `evidence/continuation-31-44/day-44.json`. Include one short teach-back (or one architecture/ADR sketch) and answer one prompt: “What invariant does this change protect, where is it enforced, and what observable response proves it?” Include actual DSA attempt/review dates only after doing the work; forecasts are not completion evidence.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 44` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
