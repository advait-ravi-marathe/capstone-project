# Day 84 — 2027-01-11: Integrate recovery evidence and independent assessment

## Contract

**Learner-owned seam:** `Independent evidence review; no feature`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Run installed selectors; record known TODOs/blockers; explain bounds/recovery; keep outcome axes separate.

### Acceptance and expected TODO state

This is an instructor evidence/contract check, not a learner implementation acceptance test. Record learner implementation and integration outcomes separately; do not infer completion from this selector.

**Instructor contract-integrity test:** `src/test/java/assignments/renewal/Day84ContractInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=Day84ContractInfrastructureTest test`
**Batch contract-integrity test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Batch command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

Independent evidence review; no feature.

## Concept/course allocation (25 minutes)

Read the primary source for 10 minutes; sketch the invariant/data flow for 10 minutes; teach it back in 5 minutes. These are planner allocations, not setup tasks.

- [Java jcmd — thread dump diagnostics](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html)

**Design sketch:** Summarize invariant, failure mode, tradeoff and integration evidence.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-84.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-11`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
