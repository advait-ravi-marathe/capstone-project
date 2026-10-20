# Day 83 — 2027-01-10: Prove packaged browser recovery under a real database lock

## Contract

**Learner-owned seam:** `Day83 JAR + browser + PostgreSQL lock harness`
**Status:** Fixed instructor integration harness installed; learner implementation remains pending.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Start the built local application JAR against disposable PostgreSQL and drive its real read-only renewal-preview route from Chromium. In a test-owned transaction, hold an `ACCESS EXCLUSIVE` lock on the preview's policy table. Issue two browser-backed preview requests and observe both in PostgreSQL `pg_stat_activity` waiting on a lock before issuing a third. The third request must return HTTP 503 `PREVIEW_BUSY` while both holders remain blocked, without a third Hikari checkout or extra PostgreSQL waiter. No response-latency threshold is used as a correctness assertion; watchdogs only fail a hung test. Release the lock in `finally`, verify both admitted requests recover, the third request presents an accessible busy state, a single explicit browser retry recovers, bounded metrics record route outcomes through the authenticated local-demo Actuator observer, and compare the policy, lifecycle-event and idempotency rows before and after. No test-only fault route or production test hook.

### Acceptance and expected TODO state

The fixed multi-process acceptance selector is installed and tagged `learnerAcceptance`. It runs the actual packaged application JAR, Chromium, and disposable PostgreSQL. It remains expected red until prerequisite learner seams are implemented. The default instructor suite runs without learner-tagged acceptance selectors; the separate infrastructure selector checks contract shape.

**Learner acceptance test:** `src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java`
**Commands:** `npm --prefix frontend ci`; `npm --prefix frontend run build:local`; `./mvnw -Plocal-app -DskipTests package`; `./mvnw -Plearner-tests -Plocal-app -Dtest=Day83PackagedLockBrowserAcceptanceTest test` (run in order). The broad packaged-route/security smoke is likewise a learner-tagged integration: `./mvnw -Plearner-tests -Plocal-app -Dtest=LocalPackagedRuntimeBoundaryAcceptanceTest test`.
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java` (contract shape only, not the real lock integration test)
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

The harness must use real process/package/browser/database boundaries and fixed synthetic rows. Do not substitute `UPDATE`, a mock lock, an app-level blocking call, or a test-only HTTP fault endpoint. Poll PostgreSQL's observed lock-wait state with a finite watchdog used only to fail a hung test. Use the learner-completed Day 48 owner-scoped SQL without duplicating it. Temporary conforming witnesses may be used only for local integration validation and must be restored before publication. The only added HTTP observability is Day 80's authenticated loopback-only local-demo metrics endpoint with a fixed name/tag allow-list; it is read-only and separate from business routes.

## Concept/course allocation (25 minutes)

Read the primary source for 10 minutes; sketch the invariant/data flow for 10 minutes; teach it back in 5 minutes. These are planner allocations, not setup tasks.

- [PostgreSQL explicit locking — ACCESS EXCLUSIVE mode](https://www.postgresql.org/docs/18/explicit-locking.html)
- [PostgreSQL `pg_locks` — `granted=false` waiting rows](https://www.postgresql.org/docs/18/view-pg-locks.html)
- [PostgreSQL monitoring stats — `pg_stat_activity` wait events](https://www.postgresql.org/docs/18/monitoring-stats.html)

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
