# Day 69 — Separate logout from restart causality

**Study: 25 minutes.** Spring Security logout → “Understanding Logout's Architecture”; PostgreSQL 18 Read Committed statement snapshots; then trace the two-journey parcel model.

## Worked example

The runnable parcel model in [`parcel_restart_reference.py`](../../../../scripts/teaching/parcel_restart_reference.py) proves logout while process one is alive, then separately verifies a fresh active cookie immediately before replacing process one. The same database stays; the old cookie is rejected by process two, a new session sees the same preview, and a foreign member receives404. A cookie already invalidated before restart is rejected as an invalid experiment.

## Transfer

Keep owner switching/foreign concealment/logout as one journey. For restart, establish an active session and successful preview in the same server context immediately before stop; do not logout it. Retain the cookie privately, restart a distinct PID with the same database and fixed clock, verify old session401, reselect owner, and compare full preview plus DB state. Delete the temporary cookie; never record its value.

**Practice 85:** 20 predictions, 40 journeys/snapshots, 15 diagnosis, 10 limitations. **Command:** `scripts/package-sprint5-demo.sh`, then `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day69PreviewIsStableAcrossPackagedRestartAndLogout test`.
