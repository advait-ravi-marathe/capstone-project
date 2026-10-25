# Engineering unit Day 94 · Calendar study day 108 · 2027-02-10: Prove statement_timeout through the same transaction boundary

## Contract

**Engineering scope:** Real pg_sleep statement-timeout harness
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.

### Expected behavior

Run fixed test-only SELECT pg_sleep through the timed boundary and observe 57014; prove a fast control succeeds. Bounded polling and generous watchdogs prevent hangs only.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/PreviewTimeoutPostgresAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PreviewTimeoutPostgresAcceptanceTest#statementTimeoutUsesSameTransactionBoundaryAndFastControlPasses test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Separate system-design interview practice — 45 minutes of the 85-minute practice block

This is a separate interview exercise, not a capstone design assignment. Attempt the interview question before using hints, references, or AI feedback. Capstone architecture is supplied above. These 45 minutes replace part of the existing 85-minute practice block; the remaining 40 minutes are the bounded implementation slice below. This replaces, and does not supplement, the full practice allocation.

- **Question:** [Q23 — Event pipeline: ingest and process](../../interview/question-bank.md#q23). How do you preserve a durable source record while processing volume grows?
- **Interview patterns:** [event-pipeline](../../interview/pattern-library.md#event-pipeline), [backpressure](../../interview/pattern-library.md#backpressure).
- **Mock phases (45 minutes total):** 5 minutes Requirements; 5 minutes Capacity and constraints; 15 minutes Initial architecture and data model; 10 minutes Bottlenecks, reliability, scaling; 10 minutes Trade-offs, failures, follow-ups.
- **Remaining engineering slice (40 minutes total):** 5 minutes preconditions and prediction; 25 minutes implementation or observation; 10 minutes selector and blocker record.
- **Bounded capstone task:** Run the statement-timeout selector to its first real learner boundary. Predict the SQLSTATE and fast-control result; implement only the named transaction-local budget seam during the 40-minute engineering slice.
- **Deferred acceptance:** `S7-D94-MOCK` — Carry every unmet original acceptance assertion under this ID. Resume from the first real learner TODO or blocked prerequisite; do not add practice minutes or infer completion from the interview mock.
- **Runnable acceptance command(s):**
  - `./mvnw -Plearner-tests -Dtest=PreviewTimeoutPostgresAcceptanceTest#statementTimeoutUsesSameTransactionBoundaryAndFastControlPasses test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

No exact matching course lesson was verified for this PostgreSQL/Hikari/JDBC behavior. Use the concrete worked example and focused official sources below; no unrelated video is padded in.

**Worked example (within the same cap):** Run test-only `SELECT pg_sleep(...)` beyond the local statement budget with no lock contention. Verify cancellation/rollback and a useful next read; avoid latency thresholds.

**Focused sources:**
- [PostgreSQL 18 Date/Time Functions — Delaying Execution with pg_sleep](https://www.postgresql.org/docs/18/functions-datetime.html)
- [PostgreSQL 18 Client Connection Defaults — statement_timeout](https://www.postgresql.org/docs/18/runtime-config-client.html)

Video viewing: 0:00; allow at most 20:00 for the worked example and focused reading, then 5:00 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.

**Assistant-provided architecture (what and why):** [Day 94 note](../assistant-architecture-notes.md#day-94). The note supplies capstone architecture decisions. Implement and test the named seam; no capstone design proposal or post-hoc justification is assigned.

**Teach-back:** Explain the supplied boundary in your own words and cite one observed acceptance result; no design-choice essay is required.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-94.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
