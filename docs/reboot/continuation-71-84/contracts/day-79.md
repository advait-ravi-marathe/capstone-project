# Day 79 — 2027-01-10: Wire one singleton gate around the existing route

## Contract

**Learner-owned seam:** `LocalPolicyRenewalReadStoreConfiguration.policyRenewalReadStore`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Create exactly one bounded singleton around the existing owner-scoped read-store bean. Preserve the exact delegate instance and existing SQL adapter, inject shared `PreviewAdmission` and `PreviewMetrics`, and validate the permit/pool/timeout policy during context startup. Do not instantiate one gate per request or replace the existing store.

### Acceptance and expected TODO state

The selector verifies the dedicated Sprint 6 profile, singleton identity, one decorator around `LocalApplicationComposition.compose(clock, dataSource)` and its existing owner-scoped adapter, finite configuration, and fail-fast startup for invalid capacity. One shared `LocalPolicyRenewalReadStoreConfiguration.policyRenewalReadStore` bean is imported by the Sprint 6 profile and later full local runtime, so Day 79 is implemented once. Day 86+ budget validation and the timed-store path remain excluded from Sprint 6.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewRuntimeWiringAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewRuntimeWiringAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Practice allocation (85 minutes)

Work within the existing synchronous Spring/JDBC path and Day 48 owner-scoped SQL. No broad component scan, duplicated query, request-scoped admission gate, asynchronous path, or new business route. Use the full existing practice block to wire and run the selector. Keep all three test observations: valid bounded configuration, singleton/delegate identity, and invalid configuration fails before serving requests. The shared bean is the sole Day 79 learner seam used by both package profiles; record the first failure without filling future TODOs.

## Concept/course allocation (25 minutes)

The saved catalog lists `Step 03 - Exploring Java Spring Framework Bean Scopes - Prototype and Singleton` (9:17) and `Step 04 - Comparing Prototype vs Singleton - Spring Framework Bean Scopes` (2:06), in28Minutes Official, Master Spring Boot 3 & Spring Framework 6 with Java (course 4993276; confirmed owned). They are partial foundations. The [Spring Framework bean scopes reference](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html) explains singleton sharing. Saved metadata does not claim the lessons were watched; no rewatch is required when readiness is demonstrated. All study fits in 25 minutes.

**Assistant-provided architecture note:** [Day 79](../assistant-architecture-notes.md#day-79). It preserves the existing adapter and composes one singleton decorator around it. No capstone design proposal or retrospective justification is assigned.

## Evidence allocation (10 minutes)

Record exact command, context result, bean identity/delegate observations, changed paths, assistance, and first blocker in `evidence/continuation-71-84/day-79.json`. A passing wiring test is not a packaged route pass.

## Separate DSA track

Daily DSA assignments, tests, reviews, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract has no DSA assignment.
