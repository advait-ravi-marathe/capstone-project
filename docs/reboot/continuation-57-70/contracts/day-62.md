# Day 62 — 2026-12-20: Derive policy ownership from the authenticated principal

## Contract

**Learner-owned seam:** `LocalPolicyOwnerResolver.ownerId(Authentication)`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Two synthetic sessions see only their own policy. A forged ownerId query/body value never overrides the principal; a foreign policy is concealed with the same not-found response.

## Acceptance and expected TODO state

The behavioral acceptance selector for this day is planned but its test source is not installed yet. The command below is not runnable evidence until the test is added. Do not implement learner behavior in the starter or mark the contract complete. Instructor manifest and DSA checks are separate and do not prove the day's API, security, UI or integration behavior. Record the actual command, result, and learner-owned paths in `evidence/continuation-57-70/day-62.json` only after the selector exists and is run.

**Acceptance test:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Acceptance selector:** `day62PrincipalOwnsReadsAndForgedOwnerIsIgnored`
**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=LocalSecurityAcceptanceTest#day62PrincipalOwnsReadsAndForgedOwnerIsIgnored test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [Spring Security Authentication architecture — Authentication, principal and authorities](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)

**Design sketch:** Derive policy ownership from the authenticated principal: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-62.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-20`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
