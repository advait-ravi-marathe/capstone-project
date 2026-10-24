# Assistant architecture notes: Days 85–98

These notes state the architecture supplied for the engineering exercises: what is used and why. They are implementation guidance, not a capstone design assignment. The learner implements the named seam, runs its acceptance checks, and records the result. System-design proposals belong only to the separate interview question-bank sessions.

<a id="day-85"></a>
## Day 85 — timeout boundary map

**What:** Keep the existing two-permit admission gate outside the four-connection Hikari pool. Its 1,000 ms connection timeout governs checkout only. PostgreSQL `lock_timeout` and `statement_timeout` govern waits after checkout and are transaction-local to one owned read transaction.

**Why:** A pool checkout limit does not bound SQL execution. Rollback and reset must finish before the outer permit is returned. Day 85 records prerequisites and evidence; it adds no runtime behavior.

<a id="day-86"></a>
## Day 86 — finite budget value object

**What:** `PreviewReadBudgetPolicy` carries lock and statement budgets as integer milliseconds. Its validator rejects non-positive values, non-exact conversion to PostgreSQL integer milliseconds, and any lock budget greater than or equal to the statement budget.

**Why:** PostgreSQL zero disables a timeout, and a lock wait should be bounded before the full statement wait. These values are configuration inputs, not latency guarantees.

<a id="day-87"></a>
## Day 87 — one transaction owner

**What:** `TimedPolicyRenewalReadStore` owns one top-level REQUIRED, read-only `TransactionTemplate`. It rejects an already-active transaction before applying settings or calling the delegate. The transaction manager and `JdbcTemplate` use the raw Hikari source; only the existing hand-managed `JdbcPolicyRenewalReadStore` receives `TransactionAwareDataSourceProxy`.

**Why:** Spring's transaction-bound connection lookup lets settings and the existing adapter share one backend without changing the adapter's SQL or using `REQUIRES_NEW`.

<a id="day-88"></a>
## Day 88 — transaction-local PostgreSQL settings

**What:** `PgLocalReadBudgets.apply` binds fixed setting names and validated values as parameters to `SELECT set_config(name, value, true)` on the transaction-bound connection. It reads back the settings on that same backend for verification.

**Why:** Bound values avoid SQL concatenation, while the `true` argument scopes the setting to the current transaction so commit or rollback restores the backend baseline.

<a id="day-89"></a>
## Day 89 — compose without replacing the read adapter

**What:** The existing bounded admission decorator remains outermost. Its delegate is the timed store; the timed store wraps the unchanged owner-scoped JDBC adapter in the transaction-aware bridge.

**Why:** The admission permit must remain held through transaction completion and cleanup. One owner-scoped SQL implementation remains the source of read facts, and the decorator preserves its arguments and present/empty result.

<a id="day-90"></a>
## Day 90 — read-only rollback boundary

**What:** The supplied transaction manager enforces read-only access. The acceptance test attempts a write and injects a mapping exception, then checks that no row was added and that transaction-local settings returned to baseline.

**Why:** A framework read-only flag alone is not evidence of unchanged data. The attempted write and failure path make rollback behavior observable.

<a id="day-91"></a>
## Day 91 — cleanup before reuse

**What:** Keep transaction completion inside the timed store call and permit release outside it. Verify success, empty result, exception, commit, and rollback before reusing the outer admission capacity.

**Why:** Returning a permit while a transaction or backend remains active can cause later work to inherit leaked settings or an occupied connection.

<a id="day-92"></a>
## Day 92 — narrow failure translation

**What:** `PreviewReadFailureTranslator` inspects nested SQL exceptions and translates only SQLSTATE `55P03` and `57014`, retaining the original cause. The HTTP advice maps the resulting domain exception to the existing fixed safe unavailable response.

**Why:** `55P03` means lock-not-available and `57014` means query-canceled; neither code alone proves a unique cancellation source. Unknown SQL and mapping failures remain failures and database details stay out of the response.

<a id="day-93"></a>
## Day 93 — real relation-lock timeout

**What:** A separate administrative connection holds `ACCESS EXCLUSIVE` on the disposable fixture table. The real timed owner-scoped reader is observed in PostgreSQL's lock wait state until its bounded call fails; rollback and same-backend cleanup are checked before releasing the holder.

**Why:** The lock and `pg_stat_activity` evidence prove the server-side wait and cleanup boundary. A stopwatch or mocked exception cannot establish that integration behavior.

<a id="day-94"></a>
## Day 94 — statement timeout without lock contention

**What:** Run test-only `SELECT pg_sleep(...)` through the timed read boundary, observe query cancellation, then execute a fast control read on the reused backend.

**Why:** With no relation lock, the experiment isolates `statement_timeout` from `lock_timeout`; the fast control proves the connection remains usable after rollback.

<a id="day-95"></a>
## Day 95 — isolated JDBC cancellation experiment

**What:** In a test-only worker, retain ownership of its JDBC `Statement`, transaction, rollback, and close. A separate observer lets `Future.get(timeout)` return while server work is still active, then requests cancellation on that exact observed statement and joins worker completion before reborrow.

**Why:** Timing out the caller's wait does not stop the JDBC operation. Cancellation ownership belongs to the worker/statement boundary in this experiment; no production cancellation registry or HTTP-to-JDBC bridge is added.

<a id="day-96"></a>
## Day 96 — same backend after every outcome

**What:** A one-slot pool makes reborrow deterministic. Capture PID, `backend_start`, and timeout settings; after success, lock timeout, statement timeout, and mapping failure, verify the same backend is idle, outside a transaction, and back at baseline settings. Reserve the final 25 minutes of today's existing practice allocation for a fresh package and Start-Class check for Day 97.

**Why:** This detects session-level leaks and incomplete cleanup that a fresh-connection assertion would miss. The package handoff tests the upcoming runtime boundary explicitly; it is only preparation, not packaged browser acceptance.

<a id="day-97"></a>
## Day 97 — packaged browser recovery

**What:** Build a fresh executable local JAR with its normal frontend assets, start that JAR against disposable PostgreSQL at loopback with a fixed test clock, select the synthetic owner in the browser, and exercise the real lock/unavailable/manual-retry journey.

**Why:** The test observes browser, HTTP, process, and database boundaries together. It verifies the safe error before releasing the lock, then one explicit retry and unchanged lifecycle rows. This is local synthetic evidence, not deployment or production security evidence. The run still depends on prior learner seams, including Day 79 and Days 86–92.

<a id="day-98"></a>
## Day 98 — evidence review, no feature

**What:** Use the installed selectors and their actual outcomes to classify complete, blocked, failed, or not-run evidence. Keep the injected lifetime defect distinguishable from the expected learner TODO.

**Why:** A successful instructor harness, forecast date, or detected synthetic defect does not prove the learner implementation. Day 98 adds no runtime behavior.
