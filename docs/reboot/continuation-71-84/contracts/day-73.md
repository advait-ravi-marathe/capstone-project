# Day 73 — 2026-12-31: Implement immediate two-permit preview admission; DSA LC 112 Path Sum

## Contract

**Learner-owned seam:** `PreviewAdmission.tryEnter`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Acquire without waiting; return empty immediately when full; accepted lease owns one release.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewAdmissionAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewAdmissionAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

PreviewAdmission.tryEnter.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **3 Questions - ThreadLocal vs Semaphore vs CountDownLatch**; exact section title: **Threads and Concurrency - Java Review (Interview) Guide** (6:23).
- Fit: partial foundation. The comparison is conceptual, not API training. Work two admitted callers and an immediately rejected third; only a lease owner releases its permit.
- Focused worked references: [Java Semaphore API — tryAcquire](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html).
- Video viewing: 6:23 at 1×; the remaining 18:37 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Draw two-permit transitions and immediate rejection.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-73.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

**DSA learner seam:** `src/main/java/assignments/dsa/PathSum.java`
**DSA fixed acceptance:** `src/test/java/assignments/dsa/PathSumAcceptanceTest.java`
**DSA command:** `./mvnw -Plearner-tests -Dtest=PathSumAcceptanceTest test`

Run the queue first, then independently attempt [LC 112 Path Sum](https://leetcode.com/problems/path-sum/). Planned first attempt: 2026-12-31; actual attempt is unset. If attempted on that date, D+3/D+7/D+21 are 2027-01-03, 2027-01-07, 2027-01-21; forecasts only.

- 25 minutes pattern learning
- 20 minutes independent solve
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-31`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
