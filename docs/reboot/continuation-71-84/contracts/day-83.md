# Engineering unit Day 83 · Calendar study day 97 · 2027-01-30: Prove packaged browser recovery under a real database lock

## Contract

**Learner-owned seam:** `Day83 JAR + browser + PostgreSQL lock harness`
**Status:** Fixed instructor integration harness installed; learner implementation remains pending.

### Expected behavior

Start the built local application JAR against disposable PostgreSQL and drive its real read-only renewal-preview route from Chromium. In a test-owned transaction, hold an `ACCESS EXCLUSIVE` lock on the preview's policy table. Issue two browser-backed preview requests and observe both in PostgreSQL `pg_stat_activity` waiting on a lock before issuing a third. The third request must return HTTP 503 `PREVIEW_BUSY` while both holders remain blocked, without a third Hikari checkout or extra PostgreSQL waiter. No response-latency threshold is used as a correctness assertion; watchdogs only fail a hung test. Release the lock in `finally`, verify both admitted requests recover, the third request presents an accessible busy state, a single explicit browser retry recovers, bounded metrics record route outcomes through the authenticated local-demo Actuator observer, and compare the policy, lifecycle-event and idempotency rows before and after. No test-only fault route or production test hook.

### Acceptance and expected TODO state

The fixed multi-process acceptance selector is installed and tagged `learnerAcceptance`. It runs the actual packaged application JAR, Chromium, and disposable PostgreSQL. It remains expected red until prerequisite learner seams are implemented. The default instructor suite runs without learner-tagged acceptance selectors; the separate infrastructure selector checks contract shape.

**Learner acceptance test:** `src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java`
**Commands:** `npm --prefix frontend ci`; `scripts/package-sprint6-demo.sh`; `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests -Dtest=Day83PackagedLockBrowserAcceptanceTest test` (run in order). The package script builds a fresh `sprint6-local` classifier JAR and receipt. The bounded Sprint 6 entry imports no Day 86+ budget validation or timed-store configuration; the selector still depends on earlier Day 72–81 learner seams.
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java` (contract shape only, not the real lock integration test)
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Practice allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

The harness must use real process/package/browser/database boundaries and fixed synthetic rows. Do not substitute `UPDATE`, a mock lock, an app-level blocking call, or a test-only HTTP fault endpoint. Poll PostgreSQL's observed lock-wait state with a finite watchdog used only to fail a hung test. Use the learner-completed Day 48 owner-scoped SQL without duplicating it. Temporary conforming witnesses may be used only for local integration validation and must be restored before publication. The only added HTTP observability is Day 80's authenticated loopback-only local-demo metrics endpoint with a fixed name/tag allow-list; it is read-only and separate from business routes.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no direct course match. No new video. Use the supplied real PostgreSQL lock, packaged-browser busy UI and explicit-retry harness. React mocks do not prove browser/database behavior; preserve the fixed instructor/learner evidence distinction.
- Focused worked references: [PostgreSQL explicit locking — ACCESS EXCLUSIVE lock](https://www.postgresql.org/docs/18/explicit-locking.html); [pg_locks system view — granted=false rows for waiting locks](https://www.postgresql.org/docs/18/view-pg-locks.html); [PostgreSQL monitoring statistics — pg_stat_activity wait_event_type and wait_event fields](https://www.postgresql.org/docs/18/monitoring-stats.html).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Assistant-provided architecture note:** [Day 83](../assistant-architecture-notes.md#day-83). Apply the stated design to the existing seam. No capstone design proposal or post-hoc architecture justification is assigned.

**Retrieval check:** Name the existing boundary and selector that observes it. This recalls supplied guidance; it is not a new design proposal or justification.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-83.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
