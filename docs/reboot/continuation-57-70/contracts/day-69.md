# Day 69 — 2026-12-31: Prove restart, logout, foreign-owner and no-write behavior

## Contract

**Learner-owned seam:** `Local runtime security and preview acceptance`
**Status:** Contract defined; learner implementation not attempted.

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

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 21 - Understanding Spring Security Authentication**; exact section title: **Securing Spring Boot Applications with Spring Security** (4:17).
- Exact lesson title: **Step 22 - Exploring Spring Security Authorization**; exact section title: **Securing Spring Boot Applications with Spring Security** (9:47).
- Fit: partial foundation. The lessons cover auth concepts, not session invalidation or restart persistence. Trace the supplied cookie-session logout and foreign-owner scenarios in the packaged browser harness; compare complete PostgreSQL snapshots and never treat JWT examples as equivalent.
- Focused worked references: [Spring Security logout handling — logout architecture and URI customization](https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html); [PostgreSQL transaction isolation — Read Committed statement snapshots](https://www.postgresql.org/docs/14/transaction-iso.html).
- Video viewing: 14:04 at 1×; the remaining 10:56 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Prove restart, logout, foreign-owner and no-write behavior: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-69.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
