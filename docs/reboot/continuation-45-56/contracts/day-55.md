# Day 55 — 2026-12-13: Connect the React preview to Spring and PostgreSQL

## Contract

**Learner-owned seam:** `browser → React → GET renewal preview`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier; engineering remains120 minutes:25 study,85 practice/testing,10 evidence.

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

## Practice and evidence

**85-minute practice allocation:**
- 10 minutes: build and verify prior gates.
- 45 minutes: run the real-browser integration.
- 20 minutes: record the three actual acceptance observations.
- 10 minutes: explain transport versus UI versus database evidence.

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

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 55` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
