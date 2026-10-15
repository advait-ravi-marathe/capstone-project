# Amazon 30-day interview sprint

A practice repository for the two selected roles, on the separate `amazon-30day-interview-sprint` branch. CoverStory implementation remains on its own branch and is not changed here.

- **Dates:** 16 October–14 November 2026, inclusive.
- **Daily time:** one 60-minute SDE II assignment (job **10579529**) and one 60-minute FBA Front-End Engineer II assignment (job **10575321**), totaling 120 minutes. The remaining 30 minutes under the daily ceiling are unallocated.
- **Resume CoverStory/Scrum:** 15 November 2026.
- **Contract source:** [`SPRINT-MANIFEST.json`](SPRINT-MANIFEST.json); per-role guides in [`assignments/`](assignments/).

## Setup and checks

Requires a Java 21 JRE with the `jdk.compiler` module, Python 3.10+, Node 20+, and npm. A separate `javac` executable is not required. No service, credentials, or real customer data is needed.

```bash
make validate        # dates, job IDs, maps, synchronized synthetic fixtures, TODO surfaces
make scaffold        # Java compile + recognized TODO check + SQL fixture/schema check
make build-frontend  # production React bundle
make test-frontend   # 12 React component/async tests; expected to pass
make test-frontend-browser # real Chromium smoke at 390 × 844; expected to pass
make test-sde        # Java acceptance suite; expected 87 recognized TODO cases
make test-sql        # SQLite SQL learner contract; expected recognized TODO
make test-frontend-contracts # fixture-driven learner helper cases; expected TODO failures
```

If `npm ci` fails because your environment cannot create its default npm cache directory, use `npm ci --cache /tmp/amazon-sprint-npm-cache`. Normal local setup is `cd frontend && npm ci`, followed by `npx playwright install chromium` for the browser test. In this Linux executor, run the browser test as `CHROMIUM_PATH=/usr/bin/chromium make test-frontend-browser`.

For the first day, use `make test-sde-day DAY=1` and `make test-frontend-day DAY=1`; each compiles/runs only the Day 1 contract and reports the initial TODO. Edit `sde/src/main/java/amazon/sprint/LearnerExercises.java` and `frontend/src/assignments.js`, then rerun those exact commands. The FE workspace is `frontend/src/ui.jsx`; `make test-frontend` exercises the actual components, injected async ports, and learner-function outputs.

The starter infrastructure checks are separate from learner outcomes. `make scaffold`, `make validate`, component tests, browser smoke, and production build verify compilation, navigation, fixture synchronization, and working async UI examples. The Java/SQL/Front-End helper acceptance commands exercise learner TODOs and are expected to report those TODOs until you implement them. Passing setup tests alone does not show independent mastery.

## Daily format

Each role: 35 minutes solving and testing, 15 minutes design/review, 10 minutes truthful STAR practice. Every prompt includes an exact file, command, synthetic cases, and an invitation to add an adversarial case. See [`docs/design-star-rubrics.md`](docs/design-star-rubrics.md) for manual self-review criteria and [`docs/source-mapping.md`](docs/source-mapping.md) plus its CSV for one reviewed reference row per role/day. Public interview accounts are self-reported anecdotes, not evidence that a question repeats.

All fixtures are synthetic. This plan does not claim to describe Amazon's internal systems, predict interview outcomes, or represent an official interview schedule. Do not mark an assignment complete unless you solved and reviewed it yourself.
