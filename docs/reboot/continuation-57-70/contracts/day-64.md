# Day 64 — 2026-12-26: Trace the supplied thin local policy issue adapter

## Contract

**Instructor-provided scaffold to inspect:** `LocalPolicyIssueController.issue(PolicyIssueRequest, Authentication)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

A valid selected synthetic quote is issued through the existing lifecycle rules and idempotency service. A same-key replay returns the existing policy; different intent conflicts. The adapter does not recalculate price, accept renewal or add lifecycle state.

## Acceptance and expected TODO state

This feature is already supplied as instructor scaffolding. Its selector verifies setup only; it is not learner implementation acceptance or evidence of learner mastery. Complete the explain/debug task below.

**Acceptance test:** `src/test/java/assignments/local/LocalApplicationAcceptanceTest.java`
**Acceptance selector:** `day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot`
**Scaffold verification command:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalApplicationAcceptanceTest#day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner task — explain/debug acceptance

Run the adapter selector and trace POST /api/policies from HTTP mapping through owner resolution, idempotency status decision, lifecycle service, and response. Explain why atomicity/idempotency belongs in the domain service; this supplied adapter is not a learner TODO.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Learner-first task proposal (5 minutes within the 85-minute practice block)

Before tracing the adapter, predict receipt lookup, service, and persistence call counts for anonymous 401, replay 200, conflict 409, and create 201.

Write the prediction before opening references, worked examples, hints, or AI. Spend the remaining 80 minutes on the existing acceptance only; this proposal is not completion evidence. Keep any unmet acceptance open as **S5-D64-ADAPTER**.

## Selector limits and open evidence

- The selected test directly invokes the controller with mocks and covers create 201 delegation only. It does not exercise HTTP mapping, unauthenticated 401, replay 200, conflict 409, zero-persist branches, or database atomicity.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 09 - Implementing POST Method to create User Resource**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (8:36).
- Exact lesson title: **Step 10 - Enhancing POST Method to return correct HTTP Status Code and Location**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (11:24).
- Fit: partial foundation. Course examples are different domain behavior. Trace the supplied adapter boundary: authenticated principal → command → service → repository → status/JSON; identify where idempotency and validation belong without copying User API contracts.
- Focused worked references: [Spring MVC controller method arguments — request-bound arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html); [Spring MVC controller return types — ResponseEntity](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html).
- Video viewing: 20:00 at 1×; the remaining 5:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Complete the task-specific proposal above before guidance; do not add a post-hoc architecture essay.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-64.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
