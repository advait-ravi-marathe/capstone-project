# Day 61 — 2026-12-23: Trace the supplied local runtime security chain

## Contract

**Instructor-provided scaffold to inspect:** `LocalSecurityConfiguration.localSecurityFilterChain(HttpSecurity, SecurityContextRepository)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Anonymous policy API requests are denied; packaged static assets are available; only the fixed local demo session path can establish one of the two synthetic identities. All writes remain CSRF-protected.

## Acceptance and expected TODO state

This feature is already supplied as instructor scaffolding. Its selector verifies setup only; it is not learner implementation acceptance or evidence of learner mastery. Complete the explain/debug task below.

**Acceptance test:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Acceptance selector:** `day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted`
**Scaffold verification command:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalSecurityAcceptanceTest#day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner task — explain/debug acceptance

Run the security selector and trace anonymous access, fixed synthetic session selection, CSRF enforcement, and the loopback guard. Explain which filter/handler owns each observed response; this is inspection of supplied setup, not a security-chain TODO.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Learner-first task proposal (5 minutes within the 85-minute practice block)

Before running the security selector, predict which layer returns anonymous GET /api/session 401, protected API 401, POST without a CSRF token 403, and authenticated unknown synthetic owner 400. Explain what a subsequent session request proves.

Write the prediction before opening references, worked examples, hints, or AI. Spend the remaining 80 minutes on the existing acceptance only; this proposal is not completion evidence. Keep any unmet acceptance open as **S5-D61-TRACE**.

## Selector limits and open evidence

- The selector checks anonymous preview denial, root availability, one synthetic session and one unknown owner. It does not prove saved-context reuse, owner-b, session rotation/logout, invalid-CSRF handler non-entry, or the complete route matrix. Its broad context may hit later TODOs.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Java Unit Testing with Spring Boot & Mockito](https://www.udemy.com/course/learn-unit-testing-with-spring-boot/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 02 - Using Mock Mvc to test Hello World Controller**; exact section title: **Section 2 - Unit Testing With Spring Boot And Mockito** (12:40).
- Exact lesson title: **Step 15 - Writing an Integration Test using @SpringBootTest**; exact section title: **Section 2 - Unit Testing With Spring Boot And Mockito** (8:51).
- Fit: partial foundation. These titles support MockMvc and application context tests, but do not guarantee the repository’s Spring Security chain. Instructor fixture provides the chain; compare authenticated and anonymous requests, CSRF behavior, and zero service calls on denial.
- Focused worked references: [Spring Security MockMvc testing — testing with MockMvc](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/index.html).
- Video viewing: 21:31 at 1×; the remaining 3:29 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Complete the task-specific proposal above before guidance; do not add a post-hoc architecture essay.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-61.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
