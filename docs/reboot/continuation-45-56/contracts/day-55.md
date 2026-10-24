# Day 55 — 2026-12-17: Connect the React preview to Spring and PostgreSQL

## Contract

**Learner-owned seam:** `browser → React → GET renewal preview`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Use the existing real browser and Spring/PostgreSQL harness. Assert actual GET path/status/eight-field JSON and matching panel values for two distinct valid rows; assert no mutation request. The principal filter is a synthetic test collaborator, not login/session/CSRF proof.

### Readiness before the task

- Actual Days45–54 including UI
- Built frontend, JDK21/Node/Chromium; no external service

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-55.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day55RealBrowserLoadsOwnerScopedPreviewFromSpringAndPostgres test`

**Instructor infrastructure/reference commands:**
- `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Dtest=RenewalPreviewTransportInfrastructureTest#testOnlyReadOnlyBrowserTransportSurvivesSpringRestart test`

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.
## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record the actual command/output, source SHA, result or first blocker, changed paths, and assistance. Do not infer learner completion from a green instructor reference or write a post-hoc architecture justification.

## Study block — exactly 25 minutes

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-55](<../../assistant-architecture-notes-15-70.md#day-55>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

- No course video is required for this day; use the supplied focused official references and project-authored worked example.
- Worked guide: [Observe a real browser read](../worked/day-55-observed-preview-demo.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [MDN Web Docs: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch) — Checking response status; Reading the response body.
- [Playwright: Response](https://playwright.dev/docs/api/class-response) — status; json.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
