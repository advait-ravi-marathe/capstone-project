# Sprint 2 interview sessions and EAI slices

Sprint 2 covers existing Days 15–28 (7–20 November 2026). Four dedicated
45-minute system-design sessions run two per week, inside the existing 85-minute
practice block. Each leaves a named 40-minute EAI slice. The ordinary daily
budget remains 25 minutes study, 85 minutes practice/testing, and 10 minutes
evidence (120 engineering minutes total); no overtime or new feature is added.
Keep the original acceptance criteria unchanged. A focused session slice never
marks the full day complete.

On each mock, write and save the initial proposal during the architecture phase
before hints, reference answers, or AI. Score with the existing 100-point
[mock rubric](mock-rubric.md), using its exact 5/5/15/10/10 phases. Keep evidence
to actual commands/results, saved proposal, assistance, and blockers. Use the
exact existing course lesson only as the listed foundation; no purchase,
enrollment, or unverified viewing is implied.

| Day/date | Mock and foundation | 40-minute EAI slice | Named deferred queue and completion check |
|---|---|---|---|
| 17 · 2026-11-09 | Q01, URL shortener: smallest useful design. Existing conditional lesson: “Making Components Reusable with Props [Core Concept]” (9:06), access unconfirmed. Review the [React state guidance](https://react.dev/learn/reacting-to-input-with-state) only inside the existing 25-minute study cap. | Render the ready-state product and accessible coverage options from the supplied typed fixture. Run `npm --prefix frontend test -- --run src/components/ProductPanel.test.tsx -t "renders product options with accessible names from its typed fixture"`. | **S2-D17-1:** Loading, error, empty, and full component acceptance remain open until `npm --prefix frontend test -- --run src/components/ProductPanel.test.tsx` passes. |
| 21 · 2026-11-13 | Q07, API gateway: route and protect. Existing owned lesson: “Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF” (11:08). The lesson is a foundation, not a substitute for the native trace. | Run `./mvnw -Dtest=Day21SecurityInfrastructureTest#serverIssuedCookieMissingAndInvalidHeadersAreRejectedBeforeWrite test`; record anonymous-valid-token, missing-token, wrong-token, valid-token statuses and handler counts. | **S2-D21-1:** The Day 21 learner selector still calls the actual Day 12 `JourneyAccessPolicy` TODO. Keep it blocked until that existing prerequisite is implemented; then run `./mvnw -Plearner-tests -Dtest=Day21ContractTest test`. Do not substitute the instructor HTTP fixture for learner acceptance. |
| 24 · 2026-11-16 | Q10, notification retries. Existing conditional lessons: “Step 82 - Performance Tuning - Measure before Tuning” (1:56) and “Step 83 - Performance Tuning - Indexes” (1:44), course access unconfirmed. If unavailable, use [PostgreSQL EXPLAIN](https://www.postgresql.org/docs/current/using-explain.html) and [Indexes](https://www.postgresql.org/docs/current/indexes.html) inside the same study block. | Implement only the named `QuoteHistoryIndexMigration` index for the existing query, then run `./mvnw -Plearner-tests -Dtest=Day24ContractTest test`. Preserve exact ordered results and record both plans; do not assert a forced planner choice or wall-clock speedup. | **S2-D24-1:** If the single selector is not passing at the end of the slice, retain its exact failure and continue only the same index/result-equivalence acceptance in the normal recovery queue. Day 24 remains pending until the selector passes. |
| 28 · 2026-11-20 | Q14, booking retry after a lost response. No new lecture is assigned; use the existing Day 28 browser/database guide and existing verified course map within 25 minutes. | Run `CHROMIUM_BIN="${CHROMIUM_BIN:?set CHROMIUM_BIN}" ./mvnw -Plearner-tests -Dtest=Day28BrowserToDatabaseTest test`; capture the actual first failed boundary and database/browser observations. Do not install or simulate a success path. | **S2-D28-1:** After actual Days 14–27 prerequisites pass, rerun the same complete browser-to-database selector and record all original acceptance results. A first-blocker trace is not a passing lifecycle. |

## Capstone implementation guidance on non-mock days

The assistant supplies the capstone architecture decision and concise rationale in each day's concept block and in the [Days 15–70 notes](../assistant-architecture-notes-15-70.md). Use the existing 85-minute practice allocation for the named implementation and acceptance. No capstone proposal, architecture choice, or post-hoc design justification is assigned. A dedicated question-set proposal remains part of each scheduled 45-minute interview mock only; unfinished acceptance stays in its named recovery queue.

## Scope limits

The SQL-versus-document, load-balancing, and frontend-state-machine entries in
the [pattern library](pattern-library.md) are comparison references. This
sprint adds no NoSQL datastore, distributed deployment, load balancer, state
machine dependency, or StreamForge implementation. CoverStory stays the primary
product; StreamForge remains gated by its existing prerequisites.
