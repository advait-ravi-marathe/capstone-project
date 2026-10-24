# CoverStory continuation — Days 71–84

This is a planning contract and instructor starter batch based on the verified
Day 70 head `db2caf3adabf11e6d619ab9a7f7e1f5aa40f2a24`. It defines a single
bounded synchronous read path for the existing synthetic HomeSecure renewal
preview. It does not edit the Scrum board or change the Day 48 learner-owned SQL.

- [Chronological manifest](manifest.json)
- [Documentation-only Scrum Story export](scrum-owner-manifest.json)
- [Dependency review](dependency-review.md)
- [Day 71–84 contracts](contracts/)
- Evidence files under `evidence/continuation-71-84/`

Scope is intentionally narrow: two nonblocking process-local permits around
the existing synchronous MVC/JDBC read, the existing Hikari pool maximum of
four with a finite one-second acquisition timeout, two sanitized capacity
errors, fixed-cardinality metrics, accessible manual retry, and recovery
verification using real pool/lock contention. No work queue, asynchronous
execution, automatic retry, fault route or extra infrastructure is added.

The engineering allocation remains 120 minutes per day: 25 study, 85 practice/testing, and 10 evidence. All learner implementations and acceptance states stay as recorded in the contracts; dates alone do not establish completion.

For a full local Maven browser run, install Playwright Chromium as in CI with `npx playwright install chromium`. If the Playwright CDN is unavailable and a compatible system Chromium is already installed, use `CHROMIUM_BIN=/usr/bin/chromium ./mvnw test` (or pass the same environment variable to the focused selector). This changes only the browser executable; it does not skip browser checks. The packaged app selector also requires the local frontend build first: `npm --prefix frontend ci && npm --prefix frontend run build:local`.

## Capstone teaching and interview practice

For each implementation task, the assistant supplies the architecture note in
`assistant-architecture-notes.md`; learners implement and test that stated
approach without a capstone design proposal or post-hoc justification. The
separate system-design interview assignments are in
[`Sprint 6 interview sessions`](../interview/sprint6-session-scope.md). Four
interviews replace 45 minutes of practice on Days 73, 76, 80, and 81; each
leaves a bounded 40-minute task slice, with the original acceptance still
required. The daily budget remains 25/85/10.
