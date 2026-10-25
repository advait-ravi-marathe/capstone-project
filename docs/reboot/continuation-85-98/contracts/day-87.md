# Day 87 — 2027-01-20: Own one top-level read-only transaction and bridge the adapter

## Contract

**Engineering scope:** TimedPolicyRenewalReadStore transaction guard and supplied connection bridge
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.

### Expected behavior

Reject an ambient transaction before mutation or delegate call. Use the supplied REQUIRED read-only TransactionTemplate, one DataSourceTransactionManager and JdbcTemplate over raw Hikari. Only unchanged JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Prove settings and delegate share backend PID and transaction.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/TimedRenewalReadAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=TimedRenewalReadAcceptanceTest#rejectsAmbientTransactionBeforeCallingDelegate test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Separate system-design interview practice — 45 minutes of the 85-minute practice block

This is a separate interview exercise, not a capstone design assignment. Attempt the interview question before using hints, references, or AI feedback. Capstone architecture is supplied above. These 45 minutes replace part of the existing 85-minute practice block; the remaining 40 minutes are the bounded implementation slice below. This replaces, and does not supplement, the full practice allocation.

- **Question:** [Q13 — Booking: reserve one seat atomically](../../interview/question-bank.md#q13). Which rows or constraints prevent two requests from reserving the final seat?
- **Interview patterns:** [transaction-boundary](../../interview/pattern-library.md#transaction-boundary), [concurrency-guards](../../interview/pattern-library.md#concurrency-guards), [data-store-choice](../../interview/pattern-library.md#data-store-choice).
- **Mock phases (45 minutes total):** 5 minutes Requirements; 5 minutes Capacity and constraints; 15 minutes Initial architecture and data model; 10 minutes Bottlenecks, reliability, scaling; 10 minutes Trade-offs, failures, follow-ups.
- **Remaining engineering slice (40 minutes total):** 5 minutes preconditions and prediction; 25 minutes implementation or observation; 10 minutes selector and blocker record.
- **Bounded capstone task:** Run the ambient-transaction guard selector to its first real learner boundary. Predict the rejected call and zero delegate calls; implement only the named transaction guard during the 40-minute engineering slice.
- **Deferred acceptance:** `S7-D87-MOCK` — Carry every unmet original acceptance assertion under this ID. Resume from the first real learner TODO or blocked prerequisite; do not add practice minutes or infer completion from the interview mock.
- **Runnable acceptance command(s):**
  - `./mvnw -Plearner-tests -Dtest=TimedRenewalReadAcceptanceTest#rejectsAmbientTransactionBeforeCallingDelegate test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

**Course (reference only; access unconfirmed):** [Master Hibernate and JPA with Spring Boot in 100 Steps — in28Minutes Official](https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps).
- **Step 58 - Introduction to Transaction Management** — section **Transaction Management**, 4:43 (283 seconds); catalog title verified, video content not audited.
Use this as transaction foundation only; it does not replace the supplied JDBC/PostgreSQL worked example.

**Worked example (within the same cap):** Trace ambient-transaction rejection before settings or delegate call; then show TransactionTemplate, JdbcTemplate, and the existing TransactionAwareDataSourceProxy adapter reaching one PostgreSQL backend. Never join or use REQUIRES_NEW.

**Focused sources:**
- [Spring programmatic transaction management — TransactionTemplate, propagation and rollback](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)
- [Spring DataSourceTransactionManager API — Thread-bound connection lookup](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceTransactionManager.html)

Video viewing: 4:43; allow at most 16:13 for the worked example and focused reading, then 4:04 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.

**Assistant-provided architecture (what and why):** [Day 87 note](../assistant-architecture-notes.md#day-87). The note supplies capstone architecture decisions. Implement and test the named seam; no capstone design proposal or post-hoc justification is assigned.

**Teach-back:** Explain the supplied boundary in your own words and cite one observed acceptance result; no design-choice essay is required.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-87.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
