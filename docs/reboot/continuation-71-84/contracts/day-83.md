# Day 83 — 2027-01-10: Prove packaged browser recovery under a real database lock

## Contract

**Learner-owned seam:** `Day83 JAR + browser + PostgreSQL lock harness`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Start the built local application JAR against disposable PostgreSQL and drive its real read-only renewal-preview route from Chromium. In a test-owned transaction, hold an `ACCESS EXCLUSIVE` lock on the preview's policy table. Issue two browser-backed preview requests and observe both in PostgreSQL `pg_stat_activity` waiting on a lock before issuing a third. The third request must return HTTP 503 `PREVIEW_BUSY` immediately without a JDBC checkout. Release the lock in `finally`, verify both admitted requests and a user-triggered browser retry recover, and compare the policy, lifecycle-event and idempotency rows before and after. No fault route or production test hook.

### Acceptance and expected TODO state

No runnable fixed acceptance selector is installed for this multi-process browser milestone yet; do not mistake the planning contract for executable coverage. The independent infrastructure selector checks contract shape only and never counts as learner completion.

**Learner acceptance test:** not installed. The current evidence records this harness as blocked until its exact packaged-JAR/browser/real-lock implementation is added after prerequisite learner seams pass.
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC, a process-local gate of 2, and the existing Hikari maximum of 4. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added endpoint, schema change, test-only fault route, or production test hook. Preserve the owner-scoped Day 48 query and learner-owned SQL; do not edit it. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

The harness must use real process/package/browser/database boundaries and fixed synthetic rows. Do not substitute `UPDATE`, a mock lock, a sleep, an app-level blocking call, or a test-only HTTP fault endpoint. Poll PostgreSQL's observed lock-wait state with a finite watchdog used only to fail a hung test. Preserve the Day 48 SQL TODO until the learner owns it; temporary witness implementations may be used locally for validation and must be restored before publication.

## Concept/course allocation (25 minutes)

Read the primary source for 10 minutes; sketch the invariant/data flow for 10 minutes; teach it back in 5 minutes. These are planner allocations, not setup tasks.

- [PostgreSQL explicit locking — ACCESS EXCLUSIVE lock](https://www.postgresql.org/docs/18/explicit-locking.html)

**Design sketch:** Sketch lock owner, blocked sessions and release signal; watchdog only prevents hangs.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-83.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-10`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
