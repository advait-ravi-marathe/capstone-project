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

## Interview-first task proposal (5 minutes inside the existing 85-minute practice block)

Before code, hints, worked answers, or AI, save a short prediction: Write the expected browser request, status, approved response fields and rendered values for both fixtures; identify the observation that proves no mutation request occurred.

Use the remaining 80 minutes only for the unchanged day acceptance. Existing prerequisites still gate the work; do not infer that a prior learner gate passed.

**Relevant patterns:** [http-boundary](../../interview/pattern-library.md#http-boundary), [measure-before-scaling](../../interview/pattern-library.md#measure-before-scaling).

**Run the existing acceptance:**
- `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day55RealBrowserLoadsOwnerScopedPreviewFromSpringAndPostgres test`

The proposal is not a pass. Keep unfinished acceptance open and stop at the first prerequisite blocker or at the daily cap. Record the proposal, actual command/result or blocker, changed paths, and assistance in the existing 10-minute evidence block. No retrospective architecture essay.

## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- No course video is required for this day; use the supplied focused official references and project-authored worked example.
- Worked guide: [Observe a real browser read](../worked/day-55-observed-preview-demo.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [MDN Web Docs: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch) — Checking response status; Reading the response body.
- [Playwright: Response](https://playwright.dev/docs/api/class-response) — status; json.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
