# Day 68 — 2026-12-26: Exercise the packaged main app through browser and PostgreSQL

## Contract

**Learner-owned seam:** `Local packaged app → browser → policy API → PostgreSQL`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Playwright launches the built executable JAR, opens the canonical packaged route `/`, and clicks the real React controls for owner selection, preview, issue, readback and same-key replay. The browser acceptance asserts preview no-write, first issue HTTP 201, owner-scoped readback HTTP 200, same-request replay HTTP 200 with one policy row, and stable snapshot identity. Current Day 66 UI, Day 62 owner, Day 34–35 issue, Day 36 read and Day 49 preview TODOs can block these assertions; any HTTP 500 is an acceptance failure.

## Acceptance and expected TODO state

The fixed browser selectors now launch the packaged JAR and drive real UI controls. Preview and issue/readback/replay are separate checks so policy counts can be measured around each operation. They are expected to fail while the UI or upstream domain TODOs remain; no 500 is a pass. Day 69 owns the separate foreign-owner 404 expectation.

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

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [Playwright Page API — goto, getByRole and click](https://playwright.dev/docs/api/class-page)
- [Playwright Response API — status, json and text](https://playwright.dev/docs/api/class-response)

**Design sketch:** Exercise the packaged main app through browser and PostgreSQL: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-68.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-26`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
