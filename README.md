# CoverStory marketplace capstone

This repository is the instructor-owned setup layer for a learner-built insurance marketplace. The product is a **simulation**: insurer offers, eligibility, payments, refunds, dispatch, and cover activation are not real integrations or promises of insurance.

The project now includes a React client shell and a Spring Boot / PostgreSQL modular-monolith API shell. Catalog, Shortlist, Application, Payments, Policy, Support, business audit, and analytics have public contracts and owned PostgreSQL schemas; assessed business behavior remains learner work. Read [the architecture guide](docs/architecture/README.md) and [module contracts](docs/assignments/coverstory-module-contracts.md) before adding endpoints or tables. A module may call another module's public port, but must not read its repository directly.

## Current checkout status

The checkout includes legacy CAP-42 through CAP-45 teaching slices alongside new CoverStory infrastructure. The project input members and selected design bundle are preserved under `docs/source-inputs/` with member-level checksums. The 594 daily guides map the saved task contracts and six linked components. Forty-six Phase 1 scaffold families have instructor fixture checks; 45 have distinct learner acceptance suites, and CAP-96 is instructor-owned gate infrastructure. No assignment is marked ready or complete. Start at the [curriculum index](docs/assignments/coverstory/curriculum-index.json), [harness coverage matrix](docs/assignments/coverstory/HARNESS-COVERAGE.md), [visual reconciliation](docs/design/visual-reconciliation.md), and [coverage report](docs/COVERAGE.md).

The directly supplied Day 001 contract has a bounded CAP-40/CAP-41 instructor scaffold and separate learner acceptance suites in [CAP-40](docs/assignments/coverstory/CAP-40.md). Its learner TODOs are intentionally unfinished. Instructor self-checks include correct and deliberately defective witnesses, while the learner's time-to-completion remains unmeasured. See [historical harness evidence](docs/evidence/coverstory/CAP-40/) and the current [candidate validation receipt](docs/assignments/coverstory/evidence/harness-candidate-2026-10-14.md). The machine check independently recomputes source hashes, task and acceptance excerpts, dates, card relationships, and planned paths. The harness matrix distinguishes mapped contracts, authored scaffolds, verified setup checks, assignment readiness, and current-candidate learner results.

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
./mvnw spring-boot:run
cd web && npm ci && npm run dev
```

The browser shell uses React Query for owner/session-scoped memory caches and revalidates `/api/session` on refresh. The API explicitly returns `401` for `/api/session` until an authentication adapter exists and `501` for `/api/catalog` until learner catalog logic is implemented; it does not invent an authenticated owner or synthetic offer results. `web/package-lock.json` pins the frontend dependency graph.

## Instructor infrastructure checks

```sh
python3 scripts/check_scaffold.py
./scripts/test_field_cipher.sh
cd web && npm run test:security
```

## Learning integrity

Instructor-owned setup includes architecture notes, synthetic fixtures, repeatable local checks, and assignment contracts. Assessed business logic remains learner-authored. AI-assisted implementation must be labelled as assisted, and passing tests alone does not establish independent mastery. Do not put real consumer, health, payment, or authentication data in fixtures.
