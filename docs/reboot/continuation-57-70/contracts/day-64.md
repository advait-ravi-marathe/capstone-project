# Day 64 — 2026-12-22: Add the thin local policy issue adapter

## Contract

**Instructor-provided scaffold to inspect:** `LocalPolicyIssueController.issue(PolicyIssueRequest)`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

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

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [Spring MVC Handler Method Arguments — @RequestBody and java.security.Principal argument resolution](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html)
- [Spring MVC @RequestBody — Reading the request body through message conversion](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html)

**Design sketch:** Add the thin local policy issue adapter: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-64.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-22`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
