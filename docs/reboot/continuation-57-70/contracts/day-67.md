# Day 67 — 2026-12-29: Inspect the disposable packaged runtime lifecycle

## Contract

**Instructor-provided scaffold to inspect:** `scripts/package-local-demo.sh and scripts/local-demo.sh`
**Status:** Scripts and runtime are instructor scaffolding; learner run/inspection evidence is pending.

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

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 09 - Get Production Ready with Spring Boot - 1 - Profiles**; exact section title: **Getting Started with Spring Boot** (7:05).
- Fit: partial foundation. Profiles are configuration context, not packaging instruction. Follow the supplied package/local-demo scripts and inspect the generated JAR; confirm the disposable process/database lifecycle without deploying or creating cloud resources.
- Focused worked references: [Spring Boot executable jar packaging — repackage goal and executable archives](https://docs.spring.io/spring-boot/maven-plugin/packaging.html).
- Video viewing: 7:05 at 1×; the remaining 17:55 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Package, start and safely reset the local demo: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-67.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
