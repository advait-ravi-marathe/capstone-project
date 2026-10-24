# Day 65 — 2026-12-27: Verify CSRF enforcement and rejected writes

## Contract

**Instructor-provided scaffold to inspect:** `LocalSecurityConfiguration security + LocalPolicyIssueController`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Missing or invalid CSRF tokens return 403 and do not change any lifecycle row. A same-origin request with a valid synthetic session and token reaches the issue adapter.

## Acceptance and expected TODO state

**Acceptance harness:** prerequisite-gated packaged learner proof

Checks authenticated missing and invalid tokens both stop with 403 before controller/service entry and preserve all policy/event/idempotency rows; anonymous plus valid token is 401. A valid session plus valid token then reaches the actual issue path; any earlier unresolved domain TODO remains an honest blocker, not a positive pass.

**Acceptance selector:** `day65InvalidCsrfCannotCreatePolicyRows`
**Acceptance source:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest#day65InvalidCsrfCannotCreatePolicyRows test
```

Stop at the first actual prerequisite TODO and record the reached boundary. A valid positive path is not green merely because its negative controls passed.

## Learner task — inspect supplied CSRF checks

Reproduce missing-token, wrong-token, and valid-token requests; record HTTP responses, handler/service counters, and full row snapshots. This supplied protection is not a learner TODO; no architecture rationale is requested.

Capture the observed status/counter/snapshot trace in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Task boundary

Inspect the supplied CSRF boundary and record the missing, wrong, and valid-token controls. Do not disable or redesign CSRF protection.

Keep unmet acceptance open as **S5-D65-CSRF**.
## Selector limits and open evidence

Checks authenticated missing and invalid tokens both stop with 403 before controller/service entry and preserve all policy/event/idempotency rows; anonymous plus valid token is 401. A valid session plus valid token then reaches the actual issue path; any earlier unresolved domain TODO remains an honest blocker, not a positive pass. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-65](<../../assistant-architecture-notes-15-70.md#day-65>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF**; exact section title: **Securing Spring Boot Applications with Spring Security** (11:08).
- Exact lesson title: **Step 09 - Exploring Spring Security - CSRF for REST API**; exact section title: **Securing Spring Boot Applications with Spring Security** (8:40).
- Fit: partial foundation. CSRF is cookie-session trust protection. Reproduce the supplied rejected write without a token and the allowed write with the expected token; never use the course’s disable-CSRF lesson as implementation guidance.
- Focused worked references: [Spring Security CSRF protection — synchronizer token pattern and CSRF configuration](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).
- Video viewing: 19:48 at 1×; the remaining 5:12 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Use the supplied assistant architecture note; record the actual selector/result or blocker and assistance.

Use the supplied decision as context for the inspection; no design explanation is required.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and observed trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-65.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
