# Day 31 — 2026-11-23: Issue eligibility and quote linkage

## Contract

**Learner-owned seam:** `PolicyLifecycleService.validateIssue(request, PolicyIssueFacts)` and `PolicyLifecycleService.issue(request, authenticatedOwnerId)`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

Accept only an owned, selected, eligible synthetic HomeSecure quote; reject missing, stale, foreign-journey, foreign-version and ineligible quote references without creating a policy. The fixed facts fixture supplies authenticated owner, quote existence, quote owner, journey, quote version, selected flag, eligibility result, and expiry date; assertions cover valid, missing, foreign-owner, unselected, stale, ineligible, foreign-journey and foreign-version cases. `PolicyIssueRequest` has no owner field, so identity comes only from the authenticated server principal.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-31.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Interview-first system-design mock (45 minutes; inside the 85-minute practice block)

**Prompt:** [Q05 — CoverStory domain workflow](../../interview/question-bank.md#q05). For this interview question only, save the initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the exact [100-point rubric](../../interview/mock-rubric.md) and its 5/5/15/10/10 phases.

**Existing EAI work (40 minutes):** 40 minutes: use the supplied PolicyIssueFacts fixture to implement the owned, selected, eligible quote happy path and one explicit rejection case; run the existing Day 31 selector and preserve its first actual failure. Do not copy the instructor fixture into production.

**Runnable acceptance:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue test`. This selector may remain red at an earlier learner-owned prerequisite; record the actual first blocker. Do not weaken it or claim the fixture is learner integration.

**Deferred queue:** **S3-D31-1** — Complete the entire valid/missing/foreign-owner/unselected/stale/ineligible/foreign-journey/foreign-version matrix and prove zero policy writes for every rejection; rerun the unchanged Day 31 selector. Day acceptance stays pending until the original full criteria pass.

**Supporting source bridge:** Required owned foundation: “Step 21 - Understanding Spring Security Authentication” (4:17), item 35017628; use only as identity-boundary foundation, then focus on the existing discovery/facts worked guide and method-argument documentation.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-31](<../../assistant-architecture-notes-15-70.md#day-31>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-31-discovery-and-facts.md](../../continuation-31-44/worked/day-31-discovery-and-facts.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Step 21 - Understanding Spring Security Authentication (4:17). Optional reference-only lessons (outside today's 25-minute scheduled path): Step 22 - Exploring Spring Security Authorization. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [GOV.UK Service Manual: Plan user research for your service](https://www.gov.uk/service-manual/user-research/plan-user-research-for-your-service) — Agree research questions.
- [GOV.UK Service Manual: Writing user stories](https://www.gov.uk/service-manual/agile-delivery/writing-user-stories) — Acceptance criteria.
- [Spring Framework: Method Arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html) — java.security.Principal table entry; @RequestBody table entry.

**Study allocation (exactly 25:00 / 1,500 seconds):** verified video 4:17; focused discovery acceptance 5:00; worked example 11:43; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example, and review of the supplied decision share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 45 minutes for the named interview mock, then 40 minutes for this day’s bounded engineering slice: 5 minutes to confirm preconditions and state the first expected result, 25 minutes on the named learner seam, and 10 minutes for the named selector plus actual result/blocker evidence. This replaces the former full 85-minute implementation breakdown on this mock day only; the original acceptance remains required and unfinished work stays in the named deferred queue. Do not add time or mark the day complete from the mock or partial slice.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Acceptance focus:** Existing independent eligibility cases; actual problem assumptions and cases, not discovery quality from a schema.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-31.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
