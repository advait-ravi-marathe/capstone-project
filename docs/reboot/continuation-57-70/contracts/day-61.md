# Day 61 — 2026-12-23: Trace the supplied local runtime security chain

## Contract

**Instructor-provided scaffold to inspect:** `LocalSecurityConfiguration.localSecurityFilterChain(HttpSecurity, SecurityContextRepository)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Anonymous policy API requests are denied; packaged static assets are available; only the fixed local demo session path can establish one of the two synthetic identities. All writes remain CSRF-protected.

## Acceptance and expected TODO state

**Acceptance harness:** supplied Sprint5-scoped verification

The bounded stage chain checks anonymous handler/API responses, CSRF-protected session selection, static root, allowlisted owners, saved context on a later request, session rotation, invalid token and unknown/null/missing owner. No later runtime context is loaded.

**Acceptance selector:** `day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted`
**Acceptance source:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest#day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted test
```

## Learner task — inspect the supplied security boundary

Run the security selector and record anonymous access, fixed synthetic session selection, CSRF enforcement, the loopback guard, and the component that produced each observed response; this is inspection of supplied setup, not a security-chain TODO.

Capture the observed trace in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Task boundary

Inspect the supplied filter/session boundary and record its observed behavior. Do not change the security chain or implement a new authentication mechanism.

Keep unmet acceptance open as **S5-D61-TRACE**.
## Selector limits and open evidence

The bounded stage chain checks anonymous handler/API responses, CSRF-protected session selection, static root, allowlisted owners, saved context on a later request, session rotation, invalid token and unknown/null/missing owner. No later runtime context is loaded. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-61](<../../assistant-architecture-notes-15-70.md#day-61>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Java Unit Testing with Spring Boot & Mockito](https://www.udemy.com/course/learn-unit-testing-with-spring-boot/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 02 - Using Mock Mvc to test Hello World Controller**; exact section title: **Section 2 - Unit Testing With Spring Boot And Mockito** (12:40).
- Exact lesson title: **Step 15 - Writing an Integration Test using @SpringBootTest**; exact section title: **Section 2 - Unit Testing With Spring Boot And Mockito** (8:51).
- Fit: partial foundation. These titles support MockMvc and application context tests, but do not guarantee the repository’s Spring Security chain. Instructor fixture provides the chain; compare authenticated and anonymous requests, CSRF behavior, and zero service calls on denial.
- Focused worked references: [Spring Security MockMvc testing — testing with MockMvc](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/index.html).
- Video viewing: 21:31 at 1×; the remaining 3:29 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Use the supplied assistant architecture note; record the actual selector/result or blocker and assistance.

Use the supplied decision as context for the inspection; no design explanation is required.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-61.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
