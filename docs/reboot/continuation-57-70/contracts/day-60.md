# Day 60 — 2026-12-22: Wire owner-scoped policy reads and prove preview performs no writes

## Contract

**Learner-owned seam:** `JdbcPolicyReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The local runtime wires the existing owner-scoped policy and preview repositories. Same-owner preview reads return the approved snapshot; a foreign owner is absent and every read leaves policy rows unchanged.

## Acceptance and expected TODO state

**Acceptance harness:** learner-owned bounded contract

Calls `components.policyReadStore().findByPolicyIdAndOwnerId` first, the assigned Day 60 seam. It checks the seeded row, changed fixture values, nullable cancellation mapping, foreign/missing reads, SQL-looking literal input, and full ordered policy/event/idempotency equality after each read. The direct call is expected to stop at the learner TODO.

**Acceptance selector:** `day60OwnerScopedPreviewReadDoesNotWrite`
**Acceptance source:** `src/test/java/assignments/local/LocalApplicationAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest#day60OwnerScopedPreviewReadDoesNotWrite test
```

The selector is intentionally expected to fail at this day’s unchanged learner TODO until implementation evidence exists. This prospective harness is not completion credit.

## Interview design mock (45 minutes inside the 85-minute practice block)

**Question:** [Q13 — Booking: reserve one seat atomically](../../interview/question-bank.md#q13)

Design a one-seat reservation under concurrent requests. Compare a relational constraint/transaction with a document-store conditional write; name the invariant, duplicate/retry behavior, and the failure response. This is transfer practice only: do not add booking code or change the selected PostgreSQL implementation.

For this interview question only, save the initial proposal before opening hints, worked references, or AI. Use Requirements 5 minutes; Capacity and constraints 5; Initial architecture and data model 15; Bottlenecks, reliability, scaling 10; Trade-offs, failures, follow-ups 10. Use the [100-point rubric](../../interview/mock-rubric.md).

**Remaining task slice (40 minutes):**

- 5 minutes: preconditions and prediction.
- 25 minutes: Implement only `JdbcPolicyReadStore.findByPolicyIdAndOwnerId`. The selector begins with a direct call to `policyReadStore`, then checks changed and nullable fixtures plus foreign/missing cases. Run it against the bounded Sprint5 stage; stop at this seam’s unchanged TODO if it is not implemented.
- 10 minutes: run the named selector and record its result or first blocker. The full day acceptance above remains unchanged; unmet work stays open as **S5-D60-READ**.

## Selector limits and open evidence

Calls `components.policyReadStore().findByPolicyIdAndOwnerId` first, the assigned Day 60 seam. It checks the seeded row, changed fixture values, nullable cancellation mapping, foreign/missing reads, SQL-looking literal input, and full ordered policy/event/idempotency equality after each read. The direct call is expected to stop at the learner TODO. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-60](<../../assistant-architecture-notes-15-70.md#day-60>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 07 - Querying Data using Spring JDBC**; exact section title: **Getting Started with JPA and Hibernate with Spring and Spring Boot** (5:36).
- Fit: partial foundation. The query lesson is general. Work the existing owner-scoped read path and list which tables/rows must remain identical; preview invokes only SELECT and never commits renewal state.
- Focused worked references: [Spring JdbcTemplate reference — query operations](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html); [PostgreSQL transaction read-only setting — default_transaction_read_only](https://www.postgresql.org/docs/18/runtime-config-client.html).
- Video viewing: 5:36 at 1×; the remaining 19:24 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner-owned changed paths in `evidence/continuation-57-70/day-60.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
