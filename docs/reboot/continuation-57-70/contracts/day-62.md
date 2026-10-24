# Day 62 — 2026-12-24: Derive policy ownership from the authenticated principal

## Contract

**Learner-owned seam:** `LocalPolicyOwnerResolver.ownerId(Authentication)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Two synthetic sessions see only their own policy. A forged ownerId query/body value never overrides the principal; a foreign policy is concealed with the same not-found response.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.

**Acceptance test:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Acceptance selector:** `day62PrincipalOwnsReadsAndForgedOwnerIsIgnored`
**Learner acceptance command:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalSecurityAcceptanceTest#day62PrincipalOwnsReadsAndForgedOwnerIsIgnored test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 21 - Understanding Spring Security Authentication**; exact section title: **Securing Spring Boot Applications with Spring Security** (4:17).
- Exact lesson title: **Step 22 - Exploring Spring Security Authorization**; exact section title: **Securing Spring Boot Applications with Spring Security** (9:47).
- Fit: partial foundation. Authentication identifies the principal; authorization checks access. Work the owner lookup path where policy ownership comes from that authenticated identity, never request input. Do not substitute AWS IAM or JWT examples.
- Focused worked references: [Spring Security authentication architecture — authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html).
- Video viewing: 14:04 at 1×; the remaining 10:56 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Derive policy ownership from the authenticated principal: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-62.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
