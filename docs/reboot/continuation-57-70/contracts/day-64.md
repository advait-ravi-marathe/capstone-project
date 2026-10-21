# Day 64 — 2026-12-22: Trace the supplied thin local policy issue adapter

## Contract

**Instructor-provided scaffold to inspect:** `LocalPolicyIssueController.issue(PolicyIssueRequest)`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

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


**Design sketch:** Trace the supplied thin local policy issue adapter: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-64.json`. Leave completion and actual dates blank until the work is actually done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-12-22 --roadmap-day 64` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
