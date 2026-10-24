# Day 68 — 2026-12-30: Exercise the packaged main app through browser and PostgreSQL

## Contract

**Learner-owned seam:** `Local packaged app → browser → policy API → PostgreSQL`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Playwright launches the built executable JAR, opens the canonical packaged route `/`, and clicks the real React controls for owner selection, preview, issue, readback and same-key replay. The browser acceptance asserts preview leaves all policy rows, versions, lifecycle history, and idempotency records byte-stable, first issue HTTP 201, owner-scoped readback HTTP 200, same-request replay HTTP 200 with exactly one new policy, one lifecycle event and one idempotency record, and stable snapshot identity. Preserve every preexisting policy value/version, lifecycle event and idempotency record across the issue/readback/replay sequence. Current Day 66 UI, Day 62 owner, Day 34–35 issue, Day 36 read and Day 49 preview TODOs can block these assertions; any HTTP 500 is an acceptance failure.

## Acceptance and expected TODO state

The fixed browser selectors now launch the packaged JAR and drive real UI controls. Preview and issue/readback/replay are separate checks. Around the read-only preview, compare the full ordered contents of `training_policy_lifecycle`, `training_policy_lifecycle_event` (including versions), and `training_policy_idempotency`; a row count alone cannot detect updates or altered history. They are expected to fail while the UI or upstream domain TODOs remain; no 500 is a pass. Day 69 owns the separate foreign-owner 404 expectation.

**Acceptance test:** `src/test/java/assignments/local/LocalPackagedBrowserAcceptanceTest.java`
**Acceptance selectors:** `day68PackagedUiPreviewIsReadOnly`; `day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce`
**Learner acceptance commands:**

```sh
CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiPreviewIsReadOnly test
CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce test
```
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Testing User Interaction & State**; exact section title: **Testing React Apps (Unit Tests)** (14:00).
- Fit: partial foundation. This is component-level test guidance, not real browser-to-JAR/PostgreSQL evidence. Use the supplied packaged browser selectors and explicit database snapshots; treat the test course as optional if unavailable, and stop at the 25-minute cap.
- Focused worked references: [Playwright Page API — navigation and user interactions](https://playwright.dev/docs/api/class-page); [Playwright Response API — status and JSON response](https://playwright.dev/docs/api/class-response).
- Video viewing: 14:00 at 1×; the remaining 11:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Exercise the packaged main app through browser and PostgreSQL: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-68.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
