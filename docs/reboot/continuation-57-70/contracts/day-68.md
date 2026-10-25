# Day 68 — 2027-01-01: Exercise the packaged main app through browser and PostgreSQL

## Contract

**Learner-owned seam:** `Local packaged app → browser → policy API → PostgreSQL`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Playwright launches the built executable JAR, opens the canonical packaged route `/`, and clicks the real React controls for owner selection, preview, issue, readback and same-key replay. The browser acceptance asserts preview leaves all policy rows, versions, lifecycle history, and idempotency records byte-stable, first issue HTTP 201, owner-scoped readback HTTP 200, same-request replay HTTP 200 with exactly one new policy, one lifecycle event and one idempotency record, and stable snapshot identity. Preserve every preexisting policy value/version, lifecycle event and idempotency record across the issue/readback/replay sequence. Current Day 66 UI, Day 62 owner, Day 34–35 issue, Day 36 read and Day 49 preview TODOs can block these assertions; any HTTP 500 is an acceptance failure.

## Acceptance and expected TODO state

**Acceptance harness:** prerequisite-gated packaged learner proof

Requires a freshly built Sprint5 classifier/receipt. Complete expected preview and issue projections, numeric amount type/value, exact declared effective date, request replay identity, rendered values, row/event/receipt semantics, and full DB snapshots at before-issue/after-issue/after-read/after-replay barriers. UI/domain TODOs remain learner gates.

**Acceptance selector:** `day68PackagedUiPreviewIsReadOnly; day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce`
**Acceptance source:** `src/test/java/assignments/local/LocalPackagedBrowserAcceptanceTest.java`
**Command:**

```sh
scripts/package-sprint5-demo.sh
CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiPreviewIsReadOnly test
CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce test
```

Stop at the first actual prerequisite TODO and record the reached boundary. A valid positive path is not green merely because its negative controls passed.

The package command must succeed immediately before the packaged selector. Artifact identity is checked by the stage receipt.

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.

Keep unmet acceptance open as **S5-D68-INTEGRATION**.
## Selector limits and open evidence

Requires a freshly built Sprint5 classifier/receipt. Complete expected preview and issue projections, numeric amount type/value, exact declared effective date, request replay identity, rendered values, row/event/receipt semantics, and full DB snapshots at before-issue/after-issue/after-read/after-replay barriers. UI/domain TODOs remain learner gates. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-68](<../../assistant-architecture-notes-15-70.md#day-68>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Testing User Interaction & State**; exact section title: **Testing React Apps (Unit Tests)** (14:00).
- Fit: partial foundation. This is component-level test guidance, not real browser-to-JAR/PostgreSQL evidence. Use the supplied packaged browser selectors and explicit database snapshots; treat the test course as optional if unavailable, and stop at the 25-minute cap.
- Focused worked references: [Playwright Page API — navigation and user interactions](https://playwright.dev/docs/api/class-page); [Playwright Response API — status and JSON response](https://playwright.dev/docs/api/class-response).
- Video viewing: 14:00 at 1×; the remaining 11:00 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Use the supplied assistant architecture note; record the actual selector/result or blocker and assistance.

Use the supplied decision as context for the assigned task; no design explanation is required.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner-owned changed paths in `evidence/continuation-57-70/day-68.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
