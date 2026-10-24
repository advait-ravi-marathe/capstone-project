# Day 74 — 2027-01-05: Make preview lease release atomic and idempotent

## Contract

**Learner-owned seam:** `PreviewLease.close`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Concurrent/repeated close invokes release callback exactly once.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewLeaseAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewLeaseAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

PreviewLease.close.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 04 - Introduction to Atomic Classes - AtomicInteger**; exact section title: **More Concurrency with Concurrent Collections and Atomic Operations** (4:10).
- Fit: partial foundation. AtomicInteger is only conceptual support. Show AtomicBoolean.compareAndSet guarding close so repeated or concurrent close invokes release exactly once.
- Focused worked references: [Java AtomicBoolean API — compareAndSet](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicBoolean.html).
- Video viewing: 4:10 at 1×; the remaining 20:50 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Show how check-then-set races can double-release.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-74.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
