# Engineering unit Day 93 · Calendar study day 107 · 2027-02-09: Prove a real relation lock timeout before holder release

## Contract

**Engineering scope:** ACCESS EXCLUSIVE lock harness
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.

### Expected behavior

Hold ACCESS EXCLUSIVE on training_policy_lifecycle; observe backend PID, backend_start, state, wait_event_type, wait_event and transaction state with fresh snapshots. Keep holder locked until error is observed; prove rollback/cleanup and reuse the same one-slot backend before releasing the holder. No row UPDATE or latency threshold.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/PreviewTimeoutPostgresAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PreviewTimeoutPostgresAcceptanceTest#realRelationLockTimeoutIsObservedAndCleanupFinishesBeforeRelease test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

No exact matching course lesson was verified for this PostgreSQL/Hikari/JDBC behavior. Use the concrete worked example and focused official sources below; no unrelated video is padded in.

**Worked example (within the same cap):** Use a holder connection with a real ACCESS EXCLUSIVE relation lock and the actual owner-scoped reader. Observe PostgreSQL wait state; prove failure and rollback before holder release; cleanup in finally.

**Focused sources:**
- [PostgreSQL 18 Explicit Locking — Table-level ACCESS SHARE and ACCESS EXCLUSIVE locks](https://www.postgresql.org/docs/18/explicit-locking.html)
- [PostgreSQL 18 Client Connection Defaults — lock_timeout and statement_timeout](https://www.postgresql.org/docs/18/runtime-config-client.html)
- [PostgreSQL 18 Monitoring Statistics — pg_stat_activity wait_event and transaction fields](https://www.postgresql.org/docs/18/monitoring-stats.html)

Video viewing: 0:00; allow at most 20:00 for the worked example and focused reading, then 5:00 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.

**Assistant-provided architecture (what and why):** [Day 93 note](../assistant-architecture-notes.md#day-93). The note supplies capstone architecture decisions. Implement and test the named seam; no capstone design proposal or post-hoc justification is assigned.

**Teach-back:** Explain the supplied boundary in your own words and cite one observed acceptance result; no design-choice essay is required.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-93.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
