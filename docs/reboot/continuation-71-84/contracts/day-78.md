# Engineering unit Day 78 · Calendar study day 92 · 2027-01-25: Map busy and unavailable outcomes safely

## Contract

**Learner-owned seam:** `PreviewCapacityExceptionHandler`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Map PREVIEW_BUSY and PREVIEW_UNAVAILABLE to sanitized HTTP 503; preserve unrelated errors; do not leak causes/messages.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewCapacityHttpAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewCapacityHttpAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Practice allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

PreviewCapacityExceptionHandler.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 12 - Implementing Generic Exception Handling for all Resources**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (11:29).
- Fit: partial foundation. Map only busy/unavailable to a sanitized 503. Preserve other errors; never expose exception causes or internal messages.
- Focused worked references: [RFC 9110 HTTP Semantics — Section 15.6.4, 503 Service Unavailable](https://www.rfc-editor.org/rfc/rfc9110.html#name-503-service-unavailable).
- Video viewing: 11:29 at 1×; the remaining 13:31 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Assistant-provided architecture note:** [Day 78](../assistant-architecture-notes.md#day-78). Apply the stated design to the existing seam. No capstone design proposal or post-hoc architecture justification is assigned.

**Retrieval check:** Name the existing boundary and selector that observes it. This recalls supplied guidance; it is not a new design proposal or justification.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-78.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
