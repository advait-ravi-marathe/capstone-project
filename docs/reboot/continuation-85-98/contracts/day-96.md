# Day 96 — 2027-01-27: Verify one-slot backend baseline reset after every outcome

## Contract

**Engineering scope:** One-slot JDBC pool recovery acceptance
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.

### Expected behavior

Capture backend PID/backend_start and settings baseline. Reborrow deliberately to the same Hikari one-slot backend after success, lock timeout, statement timeout and mapping failure. Verify local settings reset and backend idle/outside a transaction.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/PreviewTimeoutPostgresAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PreviewTimeoutPostgresAcceptanceTest#oneSlotPoolReusesSameBackendAndBaselineAfterEveryOutcome test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

No exact matching course lesson was verified for this PostgreSQL/Hikari/JDBC behavior. Use the concrete worked example and focused official sources below; no unrelated video is padded in.

**Worked example (within the same cap):** Capture backend PID/backend_start and baseline settings. Run success, then hold a real ACCESS EXCLUSIVE relation lock while the one-slot reader fails; prove rollback and idle state before releasing the holder. Follow with statement-timeout and mapping-failure rollback. After every outcome deliberately reborrow the one-slot pool and assert the same PID/backend_start, reset settings, and `pg_stat_activity` idle with no open transaction.

**Focused sources:**
- [PostgreSQL 18 System Administration Functions — set_config and current_setting](https://www.postgresql.org/docs/18/functions-admin.html)
- [PostgreSQL 18 SET — SET LOCAL transaction scope](https://www.postgresql.org/docs/18/sql-set.html)
- [PostgreSQL 18 Monitoring Statistics — backend pid and backend_start](https://www.postgresql.org/docs/18/monitoring-stats.html)

Video viewing: 0:00; allow at most 20:00 for the worked example and focused reading, then 5:00 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.

**Sketch prompt:** Draw the lock holder, transaction-bound backend, local setting lifetime, cancellation/rollback owner and when the outer permit can be released.

**Teach-back:** Explain what proves same-backend use, how settings reset, and which layer owns cleanup.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-96.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
