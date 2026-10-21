# Day 83 — 2027-01-10: Prove packaged browser recovery under a real database lock

## Contract

**Learner-owned seam:** `Day83 JAR + browser + PostgreSQL lock harness`
**Status:** Fixed instructor integration harness installed; learner implementation remains pending.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

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

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no direct course match. No new video. Use the supplied real PostgreSQL lock, packaged-browser busy UI and explicit-retry harness. React mocks do not prove browser/database behavior; preserve the fixed instructor/learner evidence distinction.
- Focused worked references: [PostgreSQL explicit locking — ACCESS EXCLUSIVE lock](https://www.postgresql.org/docs/18/explicit-locking.html); [pg_locks system view — granted=false rows for waiting locks](https://www.postgresql.org/docs/18/view-pg-locks.html); [PostgreSQL monitoring statistics — pg_stat_activity wait_event_type and wait_event fields](https://www.postgresql.org/docs/18/monitoring-stats.html).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Sketch lock owner, blocked sessions and release signal; watchdog only prevents hangs.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-83.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2027-01-10 --roadmap-day 83` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
