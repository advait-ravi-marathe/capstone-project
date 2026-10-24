# Day 75 — 2027-01-06: Bound the existing owner-scoped read adapter

## Contract

**Learner-owned seam:** `BoundedPolicyRenewalReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Reject before delegate when full; preserve policy and owner IDs; release on success, empty and failure; wrap only CannotGetJdbcConnectionException.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/BoundedPolicyRenewalReadStoreAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=BoundedPolicyRenewalReadStoreAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

BoundedPolicyRenewalReadStore.findByPolicyIdAndOwnerId.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 05 - Basics of Handling Exceptions - Need for finally**; exact section title: **Introduction to Exception Handling in Java** (6:23).
- Exact lesson title: **Step 11 - Throwing a Custom Exception - CurrenciesDoNotMatchException**; exact section title: **Introduction to Exception Handling in Java** (3:54).
- Fit: partial foundation. Trace permit release on success, empty result and failure. Translate only CannotGetJdbcConnectionException; unrelated exceptions must retain their meaning, never be swallowed.
- Focused worked references: [CannotGetJdbcConnectionException Javadoc — Class description and inheritance](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/CannotGetJdbcConnectionException.html).
- Video viewing: 10:17 at 1×; the remaining 14:43 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Trace exact exception types without parsing messages.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-75.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
