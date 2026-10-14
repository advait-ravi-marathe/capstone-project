# CAP-46 — Bounded catalog review queue

**Source contract:** Day 004, card CAP-46. The exact planned source paths are retained in the daily guide. This checkout reconciles the learner API into `coverstory.modules.catalog.api`; it does not copy to the legacy `capstone.coverstory` namespace.

## Instructor setup

- Learner seam: `src/main/java/coverstory/modules/catalog/api/CAP46Seam.java`.
- Typed key fixture: `src/main/java/coverstory/modules/catalog/api/SourceVersionKey.java` (the value identity is instructor-provided so the queue assignment isolates queue policy).
- Acceptance suite: `src/test/java/coverstory/modules/catalog/CAP46AcceptanceTest.java`.
- Independent fixture/runner smoke: `src/test/java/coverstory/modules/catalog/CatalogInstructorHarnessTest.java`.
- Synthetic fixture: `fixtures/coverstory/CAP-46/synthetic-v1.json`.
- Run instructor controls: `mvn -Dtest=CatalogInstructorHarnessTest test`.
- Run learner acceptance: `mvn -Plearner-tests -Dtest=CAP46AcceptanceTest test`.

The 4 acceptance tests exercise FIFO, empty poll, full capacity, and duplicate suppression only while pending. They are tagged learner acceptance and excluded from the default green infrastructure suite. `CAP46Seam` contains only capacity validation and TODO methods; no queue implementation is provided. Infrastructure smoke verifies fixture shape and stub discovery, not solvability or completion. No timebox rehearsal has been performed.
