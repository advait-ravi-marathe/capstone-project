# CoverStory marketplace capstone

This repository is the instructor-owned setup layer for a learner-built insurance marketplace. The product is a **simulation**: insurer offers, eligibility, payments, refunds, dispatch, and cover activation are not real integrations or promises of insurance.

The project is organized as a React client and a Spring Boot / PostgreSQL modular monolith. The backend modules own their data and behavior: Catalog, Shortlist, Application, Payments, Policy, and Support. Read [the architecture guide](docs/architecture/README.md) before adding endpoints or tables. Module boundaries are enforced by ownership: a module may call another module's public application interface, but must not read its repository or tables directly.

## Current checkout status

The cloud checkout starts from a Java 21 Maven teaching scaffold (CAP-42 through CAP-45), not an existing CoverStory application. It has no React application yet. The starter's learner TODOs remain learner work. The final daily assignment contracts and source design archive are authoritative inputs but have not been materialized in this executor; see [coverage and blockers](docs/COVERAGE.md). Nothing in this repository should be read as a claim that a learner has completed an assignment.

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

The current cloud executor has Java and Python, but no Maven executable or cached Maven distribution. Python infrastructure checks are runnable there; Maven checks require Maven to be supplied by the selected environment. See [coverage](docs/COVERAGE.md) for tested evidence and precise limitations.

## Learning integrity

Instructor-owned setup includes architecture notes, synthetic fixtures, repeatable local checks, and assignment contracts. Assessed business logic remains learner-authored. AI-assisted implementation must be labelled as assisted, and passing tests alone does not establish independent mastery. Do not put real consumer, health, payment, or authentication data in fixtures.
