# Day 73 — 2026-12-31: Implement immediate two-permit preview admission; DSA LC 112 Path Sum

## Contract

**Learner-owned seam:** `PreviewAdmission.tryEnter`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Acquire without waiting; return empty immediately when full; accepted lease owns one release.

### Acceptance and expected TODO state

The learner selector is expected red at the explicit TODO until implemented. Day 83 is a future integration selector and is not installed or claimed runnable. The independent infrastructure selector checks contract shape only and never counts as learner completion.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewAdmissionAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewAdmissionAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC, a process-local gate of 2, and the existing Hikari maximum of 4. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added endpoint, schema change, test-only fault route, or production test hook. Preserve the owner-scoped Day 48 query and learner-owned SQL; do not edit it. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

PreviewAdmission.tryEnter.

## Concept/course allocation (25 minutes)

Read the primary source for 10 minutes; sketch the invariant/data flow for 10 minutes; teach it back in 5 minutes. These are planner allocations, not setup tasks.

- [Java Semaphore API — tryAcquire](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html)

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
