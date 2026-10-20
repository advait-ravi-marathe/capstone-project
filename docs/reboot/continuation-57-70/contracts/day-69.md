# Day 69 — 2026-12-27: Prove restart, logout, foreign-owner and no-write behavior

## Contract

**Learner-owned seam:** `Local runtime security and preview acceptance`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

The packaged JAR is first started so Flyway applies migrations before the first database snapshot; it is then stopped and restarted against the same disposable PostgreSQL instance. The old session cookie is rejected, a newly selected synthetic owner sees byte-for-byte identical preview JSON, and preview, owner switching, and logout leave the full ordered contents of `training_policy_lifecycle`, `training_policy_lifecycle_event` (including versions), and `training_policy_idempotency` unchanged. The other owner cannot read the fixed owner-A policy (HTTP 404).

## Acceptance and expected TODO state

The fixed browser selector launches the executable JAR and drives UI controls across two app processes. Current Day 66/49 TODOs can prevent successful preview and foreign-owner checks; the expected 404 is not replaced with 500, and no 500 is treated as pass.

**Acceptance test:** `src/test/java/assignments/local/LocalPackagedBrowserAcceptanceTest.java`
**Acceptance selector:** `day69PreviewIsStableAcrossPackagedRestartAndLogout`
**Learner acceptance command:** `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day69PreviewIsStableAcrossPackagedRestartAndLogout test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [Spring Security Handling Logouts — Understanding Logout’s Architecture and Customizing Logout URIs](https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html)
- [PostgreSQL Transaction Isolation — Read Committed statement snapshots](https://www.postgresql.org/docs/18/transaction-iso.html)

**Design sketch:** Prove restart, logout, foreign-owner and no-write behavior: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-69.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-27`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
