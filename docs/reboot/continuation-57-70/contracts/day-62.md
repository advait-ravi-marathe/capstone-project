# Day 62 — 2026-12-24: Derive policy ownership from the authenticated principal

## Contract

**Learner-owned seam:** `LocalPolicyOwnerResolver.ownerId(Authentication)` and `LocalPolicyOwnerResolver.ownerId(Authentication, String)` and `LocalPolicyOwnerResolver.ownerId(Authentication, String)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Two synthetic sessions see only their own policy. A forged ownerId query/body value never overrides the principal; a foreign policy is concealed with the same not-found response.

## Acceptance and expected TODO state

**Acceptance harness:** learner-owned bounded contract

Checks both overloads for owner-a and owner-b, forged requested values, null/unauthenticated/anonymous/unknown/blank/null-name principals, including both overload arguments. The resolver TODO remains the intended learner failure.

**Acceptance selector:** `day62PrincipalOwnsReadsAndForgedOwnerIsIgnored`
**Acceptance source:** `src/test/java/assignments/local/LocalSecurityAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest#day62PrincipalOwnsReadsAndForgedOwnerIsIgnored test
```

The selector is intentionally expected to fail at this day’s unchanged learner TODO until implementation evidence exists. This prospective harness is not completion credit.

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.

Keep unmet acceptance open as **S5-D62-OWNER**.
## Selector limits and open evidence

Checks both overloads for owner-a and owner-b, forged requested values, null/unauthenticated/anonymous/unknown/blank/null-name principals, including both overload arguments. The resolver TODO remains the intended learner failure. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-62](<../../assistant-architecture-notes-15-70.md#day-62>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 21 - Understanding Spring Security Authentication**; exact section title: **Securing Spring Boot Applications with Spring Security** (4:17).
- Exact lesson title: **Step 22 - Exploring Spring Security Authorization**; exact section title: **Securing Spring Boot Applications with Spring Security** (9:47).
- Fit: partial foundation. Authentication identifies the principal; authorization checks access. Work the owner lookup path where policy ownership comes from that authenticated identity, never request input. Do not substitute AWS IAM or JWT examples.
- Focused worked references: [Spring Security authentication architecture — authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html).
- Video viewing: 14:04 at 1×; the remaining 10:56 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Use the supplied assistant architecture note; record the actual selector/result or blocker and assistance.

Use the supplied decision as context for the assigned task; no design explanation is required.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner-owned changed paths in `evidence/continuation-57-70/day-62.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
