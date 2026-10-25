# Day 58 — 2026-12-22: Trace the dedicated local entry point and explicit composition

## Contract

**Instructor-provided scaffold to inspect:** `LocalApplicationComposition.compose(Clock, DataSource)`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The executable local entry point composes the existing Clock, configured DataSource, policy service and read-only preview adapter. It uses no test class, static mutable dependency or duplicate lifecycle rule.

## Acceptance and expected TODO state

**Acceptance harness:** supplied Sprint5-scoped verification

Uses `Sprint5LocalApplication` and the original two-argument composition. It checks Clock/DataSource identity and rejects future timeout/capacity/outbox/recovery beans. The bounded context is instructor-supplied.

**Acceptance selector:** `day58DedicatedEntryPointWiresClockAndDataSourceExplicitly`
**Acceptance source:** `src/test/java/assignments/local/LocalApplicationAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest#day58DedicatedEntryPointWiresClockAndDataSourceExplicitly test
```

## Learner task — inspect the supplied acceptance boundary

Run the bounded Sprint 5 selector and inspect the dedicated entry point → supplied two-argument LocalApplicationComposition.compose(clock, dataSource) → adapters. Record the selector result, observed dependency path, and any blocker. The supplied composition is instructor scaffolding, not learner implementation; no architecture diagram or justification is assigned.

Capture the observed trace and selector result in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Interview design mock (45 minutes inside the 85-minute practice block)

**Question:** [Q07 — API gateway: route and protect](../../interview/question-bank.md#q07)

Design a gateway boundary for two synthetic policy APIs. State what the gateway routes and protects, what remains enforced by the service, and how traffic is distributed across stateless instances. Transfer the boundary ideas to the supplied local composition; do not implement a gateway or load balancer.

For this interview question only, save the initial proposal before opening hints, worked references, or AI. Use Requirements 5 minutes; Capacity and constraints 5; Initial architecture and data model 15; Bottlenecks, reliability, scaling 10; Trade-offs, failures, follow-ups 10. Use the [100-point rubric](../../interview/mock-rubric.md).

**Remaining task slice (40 minutes):**

- 5 minutes: preconditions and prediction.
- 25 minutes: Inspect the supplied local entry point and two-argument LocalApplicationComposition.compose(clock, dataSource); record the observed Clock/DataSource identities at the adapters and the first actual blocker.
- 10 minutes: run the named selector and record its result or first blocker. The full day acceptance above remains unchanged; unmet work stays open as **S5-D58-TRACE**.

## Selector limits and open evidence

Uses `Sprint5LocalApplication` and the original two-argument composition. It checks Clock/DataSource identity and rejects future timeout/capacity/outbox/recovery beans. The bounded context is instructor-supplied. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-58](<../../assistant-architecture-notes-15-70.md#day-58>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 09 - Get Production Ready with Spring Boot - 1 - Profiles**; exact section title: **Getting Started with Spring Boot** (7:05).
- Exact lesson title: **Step 10 - Get Production Ready with Spring Boot - 2 - ConfigurationProperties**; exact section title: **Getting Started with Spring Boot** (7:31).
- Fit: partial foundation. These lessons teach configuration foundations, not the supplied assembly script. Trace the existing profiles/config properties/beans/data source and distinguish a prebuilt local runtime from new infrastructure.
- Focused worked references: [Spring Boot externalized configuration — profiles and configuration properties](https://docs.spring.io/spring-boot/reference/features/external-config.html).
- Video viewing: 14:36 at 1×; the remaining 10:24 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference, observed trace, and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-58.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
