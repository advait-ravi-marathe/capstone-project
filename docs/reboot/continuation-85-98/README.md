# CoverStory Days 85–98: transaction-local PostgreSQL read budgets

This continuation adds a bounded learning slice around the existing
synchronous owner-scoped renewal read. It preserves the four-connection Hikari
pool, two-permit outer admission gate, and 1,000 ms connection-acquisition
timeout. PostgreSQL `lock_timeout` and `statement_timeout` are local to one
owned read-only transaction and share its physical backend with the existing
JDBC adapter.

The [canonical manifest](manifest.json) defines the 14 dated contracts. The
[chronological card export](scrum-owner-manifest.json) is prepared for the
separate Scrum owner; this branch makes no board changes. Every day links an
assistant-supplied architecture note that explains what is used and why. The
learner implements and tests the named seam. System-design proposals occur
only in four separate interview-question sessions (two per week) drawn from
the question bank; they consume 45 minutes of the existing 85-minute practice
block, leaving a bounded 40-minute engineering slice. Additional in28Minutes
and Maximilian courses are reference-only where access is unconfirmed. No AWS
study is assigned because this local PostgreSQL task has no relevant
certification dependency.

## Daily time budget

Engineering is 120 minutes per day: 25 minutes study, 85 minutes implementation/testing, and 10 minutes evidence. Video, pauses, worked examples, focused reading, and teach-back fit within the study block. Carry unfinished study forward and reduce implementation scope. Days 91 and 98 remain recovery/assessment days.

## Verification boundaries

`./mvnw test` is the default instructor infrastructure suite. It includes
browser transport checks; follow the repository README browser bootstrap
(`npm ci`, both frontend builds, and pinned Playwright Chromium, or an existing
system Chromium via `CHROMIUM_BIN=/usr/bin/chromium`). Missing package, browser,
or built assets are infrastructure failures and must not be hidden by skipping
the checks.

The explicit learner selectors remain separate under `-Plearner-tests`:

```sh
./mvnw -Plearner-tests -Dtest=TimedRenewalReadAcceptanceTest test
./mvnw -Plearner-tests -Dtest=PreviewTimeoutPostgresAcceptanceTest test
./mvnw -Plearner-tests -Dtest=JdbcCancellationLabAcceptanceTest test
```

Day 96 also reserves 25 minutes of its existing practice block for the fresh
Day 97 package and Start-Class handoff. Day 97 rebuilds the archive again before
the browser journey. Both runtime profiles use the same shared Day 79 read-store
bean; there is no second Day 79 implementation hidden in the full local app.

They may fail at the named Day 86–96 learner TODOs while the instructor suite
stays green. A missing selector or browser prerequisite is not an expected
TODO failure. Day 97 is the actual packaged local-JAR/browser journey, built
fresh by `scripts/package-local-demo.sh`, with an authenticated synthetic
session and a real PostgreSQL relation lock. The Day 85–98 manifest test is
structural scaffolding evidence only; it does not prove JAR launch or browser
behavior.

## Critical boundaries

- Keep the existing bounded decorator outermost. Reject an ambient transaction
  before applying settings or invoking the delegate; do not join it or use
  `REQUIRES_NEW`.
- Use parameterized `SELECT set_config(..., true)` calls on the transaction
  connection. Do not interpolate settings values or assume `SET LOCAL name = ?`
  accepts JDBC bind parameters.
- Wait for rollback/cleanup and worker completion before the outer admission
  permit can be reused. Transaction-local settings must reset on both commit
  and rollback; prove reset by reborrowing the same one-slot test backend.
- SQLSTATE `55P03` means `lock_not_available`; `57014` means
  `query_canceled`. Neither alone proves a particular timeout or cancellation
  source. Preserve causes and keep unknown failures visible.
- Sprint 6 Day 83 uses its bounded runtime and does not depend on the Day 86+
  budget TODOs in this continuation. Sprint 7 Days 93 and 97 separately prove
  the transaction-local lock-timeout path; Day 94 isolates statement timeout
  with a fast control read.
- The cancellation exercise is test-only. `Future.get(timeout)` stops caller
  waiting; it does not cancel SQL. The worker owns statement execution,
  rollback, close, and backend reuse. No production cancellation route or
  HTTP-to-JDBC bridge is added.
