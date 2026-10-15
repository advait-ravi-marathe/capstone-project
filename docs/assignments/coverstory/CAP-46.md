# CAP-46 — Bounded catalog review queue

**Source contract:** Day 004, card CAP-46. The exact planned source paths are retained in the daily guide. This checkout reconciles the learner API into `coverstory.modules.catalog.api`; it does not copy to the legacy `capstone.coverstory` namespace.

## Instructor setup

- Learner seam: `src/main/java/coverstory/modules/catalog/api/CAP46Seam.java`.
- Typed key fixture: `src/main/java/coverstory/modules/catalog/api/SourceVersionKey.java` (the value identity is instructor-provided so the queue assignment isolates queue policy).
- Acceptance suite: `src/test/java/coverstory/modules/catalog/CAP46AcceptanceTest.java`.
- Independent queue oracle and known-bad controls: `src/test/java/coverstory/modules/catalog/CAP46InstructorInfrastructureTest.java`; `CatalogInstructorHarnessTest` retains the fixture and TODO-discovery check.
- Synthetic fixture: `fixtures/coverstory/CAP-46/synthetic-v1.json`.
- Run instructor controls: `mvn -Dtest=CAP46InstructorInfrastructureTest,CatalogInstructorHarnessTest test`.
- Run learner acceptance: `mvn -Plearner-tests -Dtest=CAP46AcceptanceTest test`.

The 4 acceptance tests exercise FIFO, empty poll, full capacity, and duplicate suppression only while pending. They are tagged learner acceptance and excluded from the default green infrastructure suite. `CAP46Seam` contains only capacity validation and TODO methods; no queue implementation is provided. The separate test-only reference replays all saved cases from fresh empty state and rejects an unbounded queue, LIFO ordering, and permanent duplicate suppression. `CAP46InstructorInfrastructureTest` runs without calling `CAP46Seam`. Day 004 instructor scaffold readiness is recorded in the [chronological readiness report](../../evidence/coverstory/phase1-days-001-010/README.md); learner completion and elapsed time remain unmeasured.
