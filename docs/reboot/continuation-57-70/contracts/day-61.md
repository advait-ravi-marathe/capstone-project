# Day 61 — 2026-12-19: Install the local runtime security chain

## Contract

**Learner-owned seam:** `LocalSecurityConfiguration.securityFilterChain(HttpSecurity)`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Anonymous policy API requests are denied; packaged static assets are available; only the fixed local demo session path can establish one of the two synthetic identities. All writes remain CSRF-protected.

## Acceptance and expected TODO state

The fixed selector is installed and currently passes as instructor scaffolding; learner behavior remains unimplemented.

**Acceptance test:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Acceptance selector:** `day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted`
**Learner acceptance command:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalSecurityAcceptanceTest#day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [Spring Security authorization — authorize HTTP requests — authorize HTTP requests and request matchers](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)

**Design sketch:** Install the local runtime security chain: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-61.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

New pattern anchor: [LC 100 Same Tree](https://leetcode.com/problems/same-tree/) (recursive structural equality for binary trees). First attempt is planned for 2026-12-19; the listed D+3/D+7/D+21 dates are conditional forecasts, not completion. Recompute all reviews from the actual first-attempt date recorded in the shared log. The public Top Interview 150 membership is not verified here.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-19`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
