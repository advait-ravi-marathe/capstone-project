# Day 34 — 2026-11-26: Issue endpoint and stable errors

## Contract

**Learner-owned seam:** `PolicyLifecycleService.issueResponse` (typed response mapping)

The MVC controller/Principal/quote-fixture adapter is test-only scaffolding; no production route or login is enabled by this day.
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

Expose issuance with 201 + Location on first creation, 200 on identical idempotent replay, 400 invalid request, 404 inaccessible quote, 409 key conflict; no success on learner TODO.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-34.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day34MapsCreateReplayAndConflictingReplayToStableHttpCodes`
**Additional MVC acceptance:** `src/test/java/assignments/reboot/Day34IssueHttpMappingAcceptanceTest.java`

**Learner MVC command:** `./mvnw -Plearner-tests -Dtest=Day34IssueHttpMappingAcceptanceTest test`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day34MapsCreateReplayAndConflictingReplayToStableHttpCodes test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-34-request-to-response.md](../../continuation-31-44/worked/day-34-request-to-response.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Step 10 - Enhancing POST Method to return correct HTTP Status Code and Location (11:24). Optional reference-only lessons (outside today's 25-minute scheduled path): Step 11 - Implementing Exception Handling - 404 Resource Not Found. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [Spring Framework: Method Arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html) — java.security.Principal table entry; @RequestBody table entry.
- [Spring Framework: Return Values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html) — HttpEntity and ResponseEntity table entry.
- [Spring Framework: MockMvc](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html) — MockMvc.

**Study allocation (exactly 25:00 / 1,500 seconds):** verified video 11:24; worked example 9:36; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 30 / 35 / 10 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Actual standalone MockMvc JSON/principal/header/body assertions using learner domain service; 201/200/400/401/404/409 and sanitized unexpected failure separated.

## Evidence allocation (10 minutes)

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
