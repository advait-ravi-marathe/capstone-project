# Day 64 — 2026-12-26: Trace the supplied thin local policy issue adapter

## Contract

**Instructor-provided scaffold to inspect:** `LocalPolicyIssueController.issue(PolicyIssueRequest, Authentication)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

A valid selected synthetic quote is issued through the existing lifecycle rules and idempotency service. A same-key replay returns the existing policy; different intent conflicts. The adapter does not recalculate price, accept renewal or add lifecycle state.

## Acceptance and expected TODO state

**Acceptance harness:** supplied Sprint5-scoped verification

Checks supplied adapter branches: 201 persists exactly once and returns the same service snapshot; 200 replay and 409 conflict persist zero times; 401 calls no receipt/service/storage collaborator. Mocked delegation does not prove database atomicity.

**Acceptance selector:** `day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot`
**Acceptance source:** `src/test/java/assignments/local/LocalApplicationAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest#day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot test
```

## Learner task — inspect adapter decisions

Run the adapter selector and record POST /api/policies mapping through owner resolution, idempotency status decision, lifecycle service, and response. This supplied adapter is not a learner TODO.

Capture the observed status/call-count trace in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Task boundary

Inspect the supplied adapter and record status/delegation results. Do not implement domain policy or add transport behavior outside the existing contract.

Keep unmet acceptance open as **S5-D64-ADAPTER**.
## Selector limits and open evidence

Checks supplied adapter branches: 201 persists exactly once and returns the same service snapshot; 200 replay and 409 conflict persist zero times; 401 calls no receipt/service/storage collaborator. Mocked delegation does not prove database atomicity. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-64](<../../assistant-architecture-notes-15-70.md#day-64>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 09 - Implementing POST Method to create User Resource**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (8:36).
- Exact lesson title: **Step 10 - Enhancing POST Method to return correct HTTP Status Code and Location**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (11:24).
- Fit: partial foundation. Course examples are different domain behavior. Trace the supplied adapter boundary: authenticated principal → command → service → repository → status/JSON; identify where idempotency and validation belong without copying User API contracts.
- Focused worked references: [Spring MVC controller method arguments — request-bound arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html); [Spring MVC controller return types — ResponseEntity](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html).
- Video viewing: 20:00 at 1×; the remaining 5:00 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Use the supplied assistant architecture note; record the actual selector/result or blocker and assistance.

Use the supplied decision as context for the inspection; no design explanation is required.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and observed trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-64.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
