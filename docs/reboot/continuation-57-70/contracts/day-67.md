# Day 67 — 2026-12-25: Inspect the disposable packaged runtime lifecycle

## Contract

**Instructor-provided scaffold to inspect:** `scripts/package-local-demo.sh and scripts/local-demo.sh`
**Status:** Scripts and runtime are instructor scaffolding; learner run/inspection evidence is pending.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

The documented workflow builds the root UI into the executable JAR, starts a loopback-only app with a unique, disposable PostgreSQL container, and prints the health-checkable URL. The database uses tmpfs and is automatically destroyed when the launcher exits or is interrupted; there is no persistence mode and this is not a user-database reset. The separate guarded fixture-reset script applies only to an explicitly confirmed database ending in `_test`.

## Acceptance and expected TODO state

The instructor scaffold-verification selector is installed and passed; that result does not establish learner completion.

**Acceptance test:** `src/test/java/assignments/local/LocalPackageInfrastructureTest.java`
**Acceptance selector:** `day67PackageIncludesNormalUiAndNoTestRoutes`
**Scaffold verification command:** `./mvnw -Plearner-tests -Dtest=LocalPackageInfrastructureTest#day67PackageIncludesNormalUiAndNoTestRoutes test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner task — explain/debug acceptance

Run the package and local-demo scripts; inspect the JAR root entry and routes, verify loopback health/session behavior, then stop the unique ephemeral demo and confirm its disposable database is removed. There is no persistent local mode.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [Spring Boot executable jar packaging — repackage goal and executable archives](https://docs.spring.io/spring-boot/maven-plugin/packaging.html)

**Design sketch:** Package, start and safely reset the local demo: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-67.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

New pattern anchor: [LC 98 Validate Binary Search Tree](https://leetcode.com/problems/validate-binary-search-tree/) (recursive lower/upper bounds). First attempt is planned for 2026-12-25; the listed D+3/D+7/D+21 dates are conditional forecasts, not completion. Recompute all reviews from the actual first-attempt date recorded in the shared log. The public Top Interview 150 membership is not verified here.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-25`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
