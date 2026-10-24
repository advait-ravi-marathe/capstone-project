# Day 60 — 2026-12-22: Wire owner-scoped policy reads and prove preview performs no writes

## Contract

**Learner-owned seam:** `LocalRenewalRepositoryConfiguration`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The local runtime wires the existing owner-scoped policy and preview repositories. Same-owner preview reads return the approved snapshot; a foreign owner is absent and every read leaves policy rows unchanged.

## Acceptance and expected TODO state

The fixed selector is installed. It currently reaches the explicit Day 48 owner-scoped renewal-read TODO; this is an expected upstream blocker, not a completed learner result.

**Acceptance test:** `src/test/java/assignments/local/LocalApplicationAcceptanceTest.java`
**Acceptance selector:** `day60OwnerScopedPreviewReadDoesNotWrite`
**Learner acceptance command:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalApplicationAcceptanceTest#day60OwnerScopedPreviewReadDoesNotWrite test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 07 - Querying Data using Spring JDBC**; exact section title: **Getting Started with JPA and Hibernate with Spring and Spring Boot** (5:36).
- Fit: partial foundation. The query lesson is general. Work the existing owner-scoped read path and list which tables/rows must remain identical; preview invokes only SELECT and never commits renewal state.
- Focused worked references: [Spring JdbcTemplate reference — query operations](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html); [PostgreSQL transaction read-only setting — default_transaction_read_only](https://www.postgresql.org/docs/14/runtime-config-client.html).
- Video viewing: 5:36 at 1×; the remaining 19:24 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Wire owner-scoped policy reads and prove preview performs no writes: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-60.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
