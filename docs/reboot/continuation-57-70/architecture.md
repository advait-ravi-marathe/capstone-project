# Days 57–70 — isolated Sprint 5 local application boundary

This revision is based on `5e5192025af6fccf15939c63fc789d3a5868da57` and covers only the existing Sprint 5 dates, December 15–28, 2026. It preserves Days 1–56 and 71–196, domain rules, DSA history, learner statuses and all learner TODOs. No board, ticket, date, account, cloud service, customer data or deployment was added.

## Runtime boundary

- The existing default `assignments.local.LocalCoverStoryApplication` and `LocalRuntimeConfiguration` keep their later runtime graph and behavior. The default graph may include later learner-owned timed-read and delivery/recovery seams; Sprint 5 tests and the packaged workflow must not boot it.
- The early-stage executable is `assignments.sprint5.Sprint5LocalApplication`, configured with explicit imports and no broad component scan. `Sprint5LocalConfiguration` uses the existing two-argument `LocalApplicationComposition.compose(Clock, DataSource)` and reuses the supplied controllers, security chain, owner-scoped adapters and lifecycle service. It does not implement the learner's domain or resolver TODOs.
- The stage is loopback-only and starts with `local,sprint5-local`. A fresh disposable database uses unchanged migrations V001–V003 with Flyway target 3. No downgrade or migration edit is used. V004/V005 and the later migration test remain unchanged.
- Maven profile `sprint5-local`, combined with `local-app`, packages a separate `-sprint5.jar` with the explicit stage `Start-Class`. The existing default local artifact remains separate. `scripts/package-sprint5-demo.sh` builds frontend assets and a fresh stage archive, records source/tree/artifact identity, and inspects the manifest and assets. The optional Docker launcher owns one unique disposable Compose project and removes only that project.
- The stage exposes health only. It does not import later admission, budget, delivery, timed-read or manual-recovery beans and does not expose test-only routes. Session identity is synthetic and process-local; no password or production identity provider is involved.
- Browser acceptance launches the classifier archive against the test's live embedded PostgreSQL instance. It does not rely on a fixed stale JAR path without an artifact-hash receipt. Day 68–69 feature journeys remain expected to stop at unresolved prior learner TODOs; an HTTP 500 is a failure.

## Learner and instructor boundaries

Days 57, 60, 62 and 63 contain learner-owned seams. Days 58, 59, 61, 64, 65 and 67 explain or inspect instructor scaffolding. Day 66 is the learner-owned normal UI. Days 68–69 are integration gates and Day 70 remains recovery and handover; green stage setup does not award learner completion.

Course allocations are 1,500 seconds of study, 85 minutes of practice/testing and 10 minutes of evidence inside the existing 120-minute engineering block. Optional lessons replace time and do not stack. No playback or course completion is inferred.

See the [daily contracts](contracts/), [complete worked references](worked/), [canonical manifest](manifest.json), and [Scrum export](scrum-owner-manifest.json). The Scrum export is a file; no board was changed.
