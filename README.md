# CoverStory marketplace capstone

This repository is the instructor-owned setup layer for a learner-built insurance marketplace. The product is a **simulation**: insurer offers, eligibility, payments, refunds, dispatch, and cover activation are not real integrations or promises of insurance.

The project now includes a React client shell and a Spring Boot / PostgreSQL modular-monolith API shell. Catalog, Shortlist, Application, Payments, Policy, Support, business audit, and analytics have public contracts and owned PostgreSQL schemas; assessed business behavior remains learner work. Read [the architecture guide](docs/architecture/README.md) and [module contracts](docs/assignments/coverstory-module-contracts.md) before adding endpoints or tables. A module may call another module's public port, but must not read its repository directly.

## Current checkout status

The checkout includes legacy CAP-42 through CAP-45 teaching slices alongside new CoverStory infrastructure. The project input members and selected design bundle are preserved under `docs/source-inputs/` with member-level checksums. The 594 daily guides map the saved task contracts and six linked components. Sixty-six Phase 1 primary scaffold families have instructor fixture checks; 59 have distinct learner acceptance suites, and CAP-96 is instructor-owned gate infrastructure. CAP-40 is ready for a bounded attempt after executor preflight; its learner timebox remains unvalidated and product integration is still partial. No learner assignment is marked complete. Start at the [curriculum index](docs/assignments/coverstory/curriculum-index.json), [harness coverage matrix](docs/assignments/coverstory/HARNESS-COVERAGE.md), [visual reconciliation](docs/design/visual-reconciliation.md), and [coverage report](docs/COVERAGE.md).

The directly supplied Day 001 contract has a bounded CAP-40/CAP-41 scaffold and separate learner acceptance suites in [CAP-40](docs/assignments/coverstory/CAP-40.md) and [CAP-41](docs/assignments/coverstory/CAP-41.md). The Java CAP-40 seam now feeds the public in-memory compare-order preview; the Python CAP-41 seam now feeds the local synthetic wording search. Both learner TODOs remain unfinished, so the live endpoints report an explicit not-implemented status until the learner completes the seam. Day 002 CAP-42/43 wiring is described in [CAP-42](docs/assignments/coverstory/CAP-42.md) and [CAP-43](docs/assignments/coverstory/CAP-43.md): SQL totals pass through the Catalog evidence port in the disposable PostgreSQL test harness; sequential identity passes through a Catalog use case while production persistence/auth remain absent. Instructor self-checks include correct and deliberately defective witnesses, while the learner's time-to-completion remains unmeasured. See [current Day 001 integration evidence](docs/evidence/coverstory/CAP-40/integration-result.json) and the [historical harness receipt](docs/assignments/coverstory/evidence/harness-candidate-2026-10-14.md), pinned to its candidate commit. The machine check independently recomputes source hashes, task and acceptance excerpts, dates, card relationships, and planned paths. The harness matrix distinguishes mapped contracts, authored scaffolds, verified setup checks, assignment readiness, and current-candidate learner results.

## Build and smoke checks

Requirements: Java 21, Maven 3.9+, Python 3.9+. The repository `./mvnw` is a local helper; it invokes system Maven or an existing cached wrapper distribution and does not download Maven.

```sh
./mvnw test
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

The visible learner acceptance suites intentionally fail until their TODOs are completed:

```sh
./mvnw -Plearner-tests test
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

If Maven is not installed, install a supported Apache Maven binary or use the official Maven Wrapper. The selected cloud executor currently has a checksum-verified Maven distribution staged under `/tmp` for this validation run. See [coverage](docs/COVERAGE.md) for test evidence and precise limits.

## Local app shells

```sh
# Terminal 1, repository root:
./mvnw spring-boot:run
# Terminal 2, repository root:
python3 -m ai.retrieval.coverstory_health_api
# Terminal 3:
cd web && npm ci && npm run dev
```

Run Spring Boot, the loopback-only CAP-41 fixture service, and Vite in separate terminals. The browser shell uses React Query for public catalog/retrieval reads and owner/session-scoped memory caches for future private data; it revalidates `/api/session` on refresh. The API returns `401` for `/api/session` until an authentication adapter exists. `GET /api/catalog` serves a bounded, health-only synthetic catalog through the Catalog module's public port; it contains no insurer wording, price, or payable quote. `POST /api/shortlist/selection-order` calls the CAP-40 shortlist seam only to order ephemeral public comparison references; it does not persist a shortlist or treat device scope as authorization. `/retrieval/health` calls the CAP-41 Python seam against the fixed synthetic fixture and refuses caller-provided source documents. Both routes return an explicit not-implemented result while their learner seams are TODOs. `web/package-lock.json` pins the frontend dependency graph.

## Instructor infrastructure checks

```sh
python3 scripts/check_scaffold.py
./scripts/test_field_cipher.sh
cd web && npm run test:security
```

## Learning integrity

Instructor-owned setup includes architecture notes, synthetic fixtures, repeatable local checks, and assignment contracts. Assessed business logic remains learner-authored. AI-assisted implementation must be labelled as assisted, and passing tests alone does not establish independent mastery. Do not put real consumer, health, payment, or authentication data in fixtures.
