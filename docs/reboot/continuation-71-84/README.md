# CoverStory continuation — Days 71–84

This is a planning contract and instructor starter batch based on the verified
Day 70 head `db2caf3adabf11e6d619ab9a7f7e1f5aa40f2a24`. It defines a single
bounded synchronous read path for the existing synthetic HomeSecure renewal
preview. It does not edit the Scrum board or change the Day 48 learner-owned SQL.

- [Chronological manifest](manifest.json)
- [Documentation-only Scrum Story export](scrum-owner-manifest.json)
- [DSA study map](dsa/tree-study-map.md)
- [Dependency review](dependency-review.md)
- [Day 71–84 contracts](contracts/)
- Evidence files under `evidence/continuation-71-84/`

Scope is intentionally narrow: two nonblocking process-local permits around
the existing synchronous MVC/JDBC read, the existing Hikari pool maximum of
four with a finite one-second acquisition timeout, two sanitized capacity
errors, fixed-cardinality metrics, accessible manual retry, and recovery
verification using real pool/lock contention. No work queue, asynchronous
execution, automatic retry, fault route or extra infrastructure is added.

All learner implementations remain unattempted. Day 83 has a fixed packaged-JAR/browser/real-PostgreSQL-lock acceptance harness. Both it and the broader packaged runtime boundary smoke are explicitly tagged learner acceptance because their app startup crosses the unfinished Day 72/79 seams; their runnable selectors remain documented in the contracts. The default Maven suite excludes `learnerAcceptance` and is the instructor infrastructure suite. The GitHub Actions workflow runs on this continuation branch and executes the default suite. The local demo exposes only read-only `/actuator/health` and `/actuator/metrics`; metrics require an authenticated synthetic-owner session and are limited by a fixed meter/tag allow-list. The demo application binds to loopback. This observability surface is separate from business routes and has no test-only fault-injection endpoint. The attempt log is empty; planned DSA reviews are not completion evidence. The two-permit gate does not reserve Hikari slots or guarantee capacity for other routes.

For a full local Maven browser run, install Playwright Chromium as in CI with `npx playwright install chromium`. If the Playwright CDN is unavailable and a compatible system Chromium is already installed, use `CHROMIUM_BIN=/usr/bin/chromium ./mvnw test` (or pass the same environment variable to the focused selector). This changes only the browser executable; it does not skip browser checks. The packaged app selector also requires the local frontend build first: `npm --prefix frontend ci && npm --prefix frontend run build:local`.
FDE alignment: See [the North Star amendment](../fde-north-star-amendment.md) and [reference-only alignment overlay](../fde-alignment.json). Existing curriculum, prerequisites, budgets, DSA history and learner status remain unchanged; this adds purpose and evidence boundaries, not completion credit.
