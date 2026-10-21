# Day 68 — Compare API, UI and persisted state

**Study: 25 minutes.** Playwright “waitForResponse”, “getByRole”, Response “json/status”; then trace the independent parcel semantic oracle.

## Worked example

The runnable parcel trace in [`parcel_snapshot_reference.py`](../../../../scripts/teaching/parcel_snapshot_reference.py) requires create/read/replay statuses, the same request identity, complete API projection, matching DOM and full before/after snapshots. It catches a same-count changed amount after readback even when the final table is later restored. It is a Python counterexample, not an insurance fixture or PostgreSQL execution.

## Transfer

Use only the issued `journey-a`/`quote-a` fixture. Compare all approved preview fields and DOM text; issue201/read200/replay200 must share the request and complete snapshot. Compare database snapshots before issue, after issue, after read, and after replay. Include the fixed request clock; never accept an HTTP500 as a pass.

**Practice 85:** 15 freeze predictions, 40 run, 20 diagnose, 10 explain. **Evidence 10:** actual HTTP, DOM and independent DB observations. **Commands:** `scripts/package-sprint5-demo.sh`, then `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day68PackagedUiPreviewIsReadOnly+day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce test`.
