# Start the first work session

This is a local learning project. The catalog, policy wording, payment flow, insurer actions, and cover activation are simulations. Use only the checked-in synthetic fixtures; do not enter real consumer or health data.

## Prerequisites

- JDK 21, including `javac` (a Java runtime alone is insufficient).
- Python 3.10 or newer.
- Node.js 22.12 or newer and npm.
- Network access to Maven Central and the npm registry for first-time dependency downloads. The checked-in Maven wrapper verifies and caches Maven 3.10.0; a separately installed Maven is optional.

Day 001 instructor tests use disposable embedded PostgreSQL binaries in test scope. No managed PostgreSQL, Kafka, Redis, cloud account, credentials, or external insurer/payment service is needed. If your environment requires a proxy, configure Maven/npm using your organization's approved settings; do not disable TLS verification.

Check the tools from the repository root:

```sh
java -version
javac -version
python3 --version
node --version
npm --version
./mvnw -version
```

## First session: Day 001, CAP-40 plus CAP-41

The combined daily limit is **150 minutes**. The capstone work, including the linked CAP-41 retrieval component, is capped at 95 minutes: 10 minutes reading, 55 minutes implementation, and 30 minutes tests/evidence. CAP-41 adds no extra scheduled minutes. The remaining source allocation is 30 minutes certification, 15 minutes DSA, and 10 minutes reflection. Record actual elapsed time; the timebox has not yet been validated against a learner attempt.

Read [CAP-40](assignments/coverstory/CAP-40.md) and [CAP-41](assignments/coverstory/CAP-41.md), then inspect the TODO seams:

- `src/main/java/coverstory/modules/shortlist/api/CAP40Seam.java`
- `ai/retrieval/coverstory_health.py`

Run the instructor setup checks separately from learner acceptance:

```sh
./mvnw -Dtest=CAP40ScaffoldInfrastructureTest test
python3 -m unittest discover -s ai/tests -p test_coverstory_cap41_infrastructure.py
python3 -m unittest ai.tests.test_coverstory_cap41_http
./mvnw -Dtest=ShortlistSelectionControllerInfrastructureTest,ApplicationRuntimeIntegrationTest test
```

These instructor checks exercise fixtures, transport, app wiring, and reference witnesses. They do not implement or complete either learner task. To see the learner acceptance contract, run it separately:

```sh
./mvnw -Plearner-tests -Dtest=CAP40AcceptanceTest test
python3 -m unittest discover -s ai/tests -p test_coverstory_cap41_acceptance.py
```

Before implementation, these are expected to stop at the explicit learner TODOs. Treat those failures as a working acceptance harness, not as a setup failure or as completed work. Implement the saved behavior, rerun the acceptance tests, and record your own result and elapsed time. Passing tests alone does not demonstrate independent mastery; label AI-assisted work and prepare the saved explain-back.

## Optional local browser walkthrough

Start each service in a separate terminal at the repository root:

```sh
# Terminal 1
./mvnw spring-boot:run

# Terminal 2
python3 -m ai.retrieval.coverstory_health_api

# Terminal 3
cd web
npm ci
npm run dev
```

Open the loopback Vite address printed by the final command. `/api/session` currently returns `401` because no authentication adapter exists. `GET /api/catalog` returns bounded synthetic health-only records; the compare and retrieval seams return `501` while their learner TODOs remain. These responses are expected. No payment, insurer dispatch, or active cover is represented.

Useful frontend checks, from `web/`:

```sh
npm run build
npm run test:security
npm run test:catalog-fixture
```

## What to do next

Follow the saved daily guides in order, keeping each day's full 150-minute allocation intact. Use the [curriculum index](assignments/coverstory/curriculum-index.json), [harness coverage](assignments/coverstory/HARNESS-COVERAGE.md), and [coverage report](COVERAGE.md) to distinguish mapped source contracts, runnable instructor scaffolds, conditional checks, and learner TODOs. Days 001–014 have instructor setup receipts (Day 015 is partial), but this is not evidence that the learner has completed them or that daily timeboxes fit. The learner-mode CAP-57/CAP-166 route checks are separate and intentionally stop at TODOs until implemented. Full end-to-end product readiness remains partial; consult each per-day receipt before treating a later task as runnable.

Keep private journey state memory-only in the browser and scoped to the current owner/session. Clear it on logout or account switch, and guard late responses. UUID journey identifiers do not grant access. Keep durable business audit separate from allowlisted, best-effort page/click analytics. All data remains synthetic.
