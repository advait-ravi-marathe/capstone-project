# Day 81 — 2027-01-08: Show an accessible manual retry state

## Contract

**Learner-owned seam:** `RenewalPreviewBusyState`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Accessible busy/unavailable status and one explicit retry; no auto retry or session reset; preserve policy context.

### Acceptance and expected TODO state

The learner selector is expected red at the explicit TODO until implemented. Day 83 is a future integration selector and is not installed or claimed runnable. The independent infrastructure selector checks contract shape only and never counts as learner completion.

**Learner acceptance test:** `frontend/src/components/RenewalPreviewBusyState.test.tsx`
**Learner command:** `cd frontend && RUN_LEARNER_ACCEPTANCE=true npx vitest run src/components/RenewalPreviewBusyState.test.tsx`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC, a process-local gate of 2, and the existing Hikari maximum of 4. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added endpoint, schema change, test-only fault route, or production test hook. Preserve the owner-scoped Day 48 query and learner-owned SQL; do not edit it. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

RenewalPreviewBusyState.

## Concept/course allocation (25 minutes)

Read the primary source for 10 minutes; sketch the invariant/data flow for 10 minutes; teach it back in 5 minutes. These are planner allocations, not setup tasks.

- [WAI-ARIA alert pattern — status/alert semantics](https://www.w3.org/WAI/ARIA/apg/patterns/alert/)

**Design sketch:** Draw state transitions and prove retry follows explicit action.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-81.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-08`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
