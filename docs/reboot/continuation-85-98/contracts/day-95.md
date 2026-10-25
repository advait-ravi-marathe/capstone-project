# Day 95 — 2027-01-28: Separate Future waiting from JDBC Statement cancellation

## Contract

**Engineering scope:** Isolated JDBC cancellation lab; no production API
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.

### Expected behavior

Future.get timeout does not stop server work. Observe the exact active JDBC Statement and backend; lab cancellation thread only requests Statement.cancel. Worker owns rollback/close and completes before reborrow/reuse. No production registry, HTTP/JDBC bridge or cancellation endpoint.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/JdbcCancellationLabAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=JdbcCancellationLabAcceptanceTest#futureTimeoutDoesNotCancelSqlAndWorkerOwnsCleanup test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

No exact matching course lesson was verified for this PostgreSQL/Hikari/JDBC behavior. Use the concrete worked example and focused official sources below; no unrelated video is padded in.

**Worked example (within the same cap):** Contrast Future.get(timeout) with server work still running. Test-only worker owns its Statement, rollback and close; cancel that observed Statement and join worker completion before reborrow.

**Focused sources:**
- [Java 21 Statement API — Statement.cancel()](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Statement.html)
- [Java 21 Future API — get(timeout, unit) versus cancel](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html)
- [PostgreSQL 18 Protocol Flow — Canceling requests](https://www.postgresql.org/docs/18/protocol-flow.html#PROTOCOL-FLOW-CANCELING-REQUESTS)
- [PostgreSQL 18 Error Codes — 57014 query_canceled](https://www.postgresql.org/docs/18/errcodes-appendix.html)

Video viewing: 0:00; allow at most 20:00 for the worked example and focused reading, then 5:00 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.

**Assistant-provided architecture (what and why):** [Day 95 note](../assistant-architecture-notes.md#day-95). The note supplies capstone architecture decisions. Implement and test the named seam; no capstone design proposal or post-hoc justification is assigned.

**Teach-back:** Explain the supplied boundary in your own words and cite one observed acceptance result; no design-choice essay is required.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-95.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
