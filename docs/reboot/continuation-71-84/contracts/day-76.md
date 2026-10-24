# Day 76 — 2027-01-07: Prove maximum concurrency and no permit leaks

## Contract

**Learner-owned seam:** `Latch-controlled acceptance`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Observe at most two delegate entries; reject third immediately; release on success, injected delegate failure, and double close. Join every worker `Future` and propagate its exception. After the injected failure, prove a subsequent call succeeds, then run a complete second admission cycle to show no permit leak.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewAdmissionConcurrencyAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewAdmissionConcurrencyAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

Use latch-controlled delegate entry and release. Assert the maximum observed delegate concurrency is two; the rejected third call never enters the delegate. Inject a delegate exception only in the test, release the held calls, and assert the exception reaches the caller and capacity recovers. Do not infer fairness or FIFO ordering from nonblocking semaphore acquisition.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **3 Questions - ThreadLocal vs Semaphore vs CountDownLatch**; exact section title: **Threads and Concurrency - Java Review (Interview) Guide** (6:23).
- Exact lesson title: **Step 11 - Executor Service - Returning a Future from Thread using Callable**; exact section title: **Introduction to Threads And Concurrency in Java** (6:43).
- Fit: partial foundation. Use latch barriers, join/get every Future, propagate worker failures, prove maximum two entries, recovery after failure and a complete second cycle. Avoid sleep-based race tests.
- Focused worked references: [Java CountDownLatch API — await/countDown and happens-before](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CountDownLatch.html).
- Video viewing: 13:06 at 1×; the remaining 11:54 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Sketch latch schedule; distinguish barrier evidence from sleeps.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-76.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
