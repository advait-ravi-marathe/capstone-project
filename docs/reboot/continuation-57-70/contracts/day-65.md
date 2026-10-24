# Day 65 — 2026-12-27: Verify CSRF enforcement and rejected writes

## Contract

**Instructor-provided scaffold to inspect:** `LocalSecurityConfiguration security + LocalPolicyIssueController`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Missing or invalid CSRF tokens return 403 and do not change any lifecycle row. A same-origin request with a valid synthetic session and token reaches the issue adapter.

## Acceptance and expected TODO state

This feature is already supplied as instructor scaffolding. Its selector verifies setup only; it is not learner implementation acceptance or evidence of learner mastery. Complete the explain/debug task below.

**Acceptance test:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Acceptance selector:** `day65InvalidCsrfCannotCreatePolicyRows`
**Scaffold verification command:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalSecurityAcceptanceTest#day65InvalidCsrfCannotCreatePolicyRows test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner task — explain/debug acceptance

Reproduce a POST without CSRF and one with a valid token, compare HTTP responses and policy row counts, then explain why rejection occurs before controller/service execution. This supplied protection is not a learner TODO.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF**; exact section title: **Securing Spring Boot Applications with Spring Security** (11:08).
- Exact lesson title: **Step 09 - Exploring Spring Security - CSRF for REST API**; exact section title: **Securing Spring Boot Applications with Spring Security** (8:40).
- Fit: partial foundation. CSRF is cookie-session trust protection. Reproduce the supplied rejected write without a token and the allowed write with the expected token; never use the course’s disable-CSRF lesson as implementation guidance.
- Focused worked references: [Spring Security CSRF protection — synchronizer token pattern and CSRF configuration](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).
- Video viewing: 19:48 at 1×; the remaining 5:12 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Verify CSRF enforcement and rejected writes: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-65.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
