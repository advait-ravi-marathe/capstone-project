# Day 68 — 2026-12-26: Exercise the packaged main app through browser and PostgreSQL

## Contract

**Learner-owned seam:** `Local packaged app → browser → policy API → PostgreSQL`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

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

- Optional replacement only: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — **Testing User Interaction & State**, section **Testing React Apps (Unit Tests)** (14:00, item `25890068`); access `unconfirmed_reference_only`. Do not purchase or enroll; replace equal study time if already accessible.

**Study sequence (seconds; total 1500):**
- official real-browser response focus: 360 seconds.
- complete independent semantic oracle example: 900 seconds.
- prediction: 240 seconds.

**Official references:**
- [Playwright: Page](https://playwright.dev/docs/api/class-page) — waitForResponse, getByRole
- [Playwright: Response](https://playwright.dev/docs/api/class-response) — json, status
- [PostgreSQL18: Transaction Isolation](https://www.postgresql.org/docs/18/transaction-iso.html#XACT-READ-COMMITTED) — Read Committed Isolation Level

**Worked guide:** [day-68.md](docs/reboot/continuation-57-70/worked/day-68.md). Read the complete supplied reference before applying the distinct native variation. This is instructor teaching, not learner completion.

**Time rule:** Optional lessons replace time inside these 1500 seconds; pauses do too. No overtime. Access and viewing are not inferred.

**Runnable acceptance commands:**
- `scripts/package-sprint5-demo.sh`
- `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiPreviewIsReadOnly test`
- `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce test`

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-68.json`. Leave completion and actual dates blank until the work is actually done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 68` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
