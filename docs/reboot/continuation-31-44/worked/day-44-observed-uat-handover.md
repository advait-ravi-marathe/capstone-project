# Day 44: Observed UAT and handover

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Synthetic parcel expectation is DELIVERED after restart; actual UI says IN_TRANSIT while fresh GET says DELIVERED. Verdict is failed. “Component cache” is only a hypothesis. Record actual request/body/UI/source and preserve the first failure; do not overwrite it after a retry passes. A fresh Spring/browser process using the same database demonstrates process-restart persistence, not a database crash test.

## Transfer task

Transfer: run only after real Day31–43 gates. The supplied bridge exercises persisted issue/endorsement, one aborted summary GET and real recovery, then cancellation and fresh Spring/browser readback on the same PostgreSQL. Compare frozen expectations to observations. If a prior TODO blocks, record the first failure and leave UAT blocked; no canned state or manually seeded success.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.

## Frozen expectation versus actual observation

The expected cases are in [`servicing-uat.expected.json`](../exercises/servicing-uat.expected.json); the blank [`servicing-uat.observation.template.json`](../exercises/servicing-uat.observation.template.json) starts at `not_run`. Freeze the expectation version/hash before the command. Actual UI, HTTP/network outcome, database row counts, source SHA, exit code, screenshots and first blocker belong in an append-only run artifact under `evidence/continuation-31-44/uat-runs/` only after an actual run. A network abort has no HTTP status. Do not copy expected values into actual fields.

The test-only summary endpoint reads by both `policyId` and the fixed synthetic `owner-a` principal, then calls the actual `readForOwner` and `toServicingView` learner seams. It maps `BigDecimal` with `toPlainString()`. No fixture summary, full private `PolicySnapshot`, owner header or expected-value fallback is returned. The browser runner scopes role queries to `region[name="Policy servicing"]`, waits on actual requests/locators rather than sleeps, and captures screen/log data. `realBrowserRoundTripSurvivesSpringServerRestart` remains infrastructure transport smoke; it does not count as this learner UAT.

Run focused infrastructure/reference commands before learner acceptance. Run `npm --prefix frontend run build` before the real browser selector. Actual Day44 prerequisites are runnable JDK21/Node/Chromium and disposable PostgreSQL plus completed real Day31–43 learner gates. At the first unmet prerequisite, preserve the exact failure and leave later scenarios blocked. A successful instructor temporary witness validates scaffold wiring only; it is removed before release and never recorded as learner evidence.
