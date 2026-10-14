# CAP-50 — Count import decisions

**Source contract:** Day 006, card CAP-50. The saved source paths use a proposed namespace; this checkout maps the API to `coverstory.modules.catalog.api` while preserving exact source text in the canonical JSON.

## Instructor setup

- Learner seam: `src/main/java/coverstory/modules/catalog/api/CAP50Seam.java` and `ImportDecision.java`.
- Acceptance suite: `src/test/java/coverstory/modules/catalog/CAP50AcceptanceTest.java`.
- Independent fixture/runner smoke: `src/test/java/coverstory/modules/catalog/CatalogInstructorHarnessTest.java`.
- Synthetic decision rows and oracle: `fixtures/coverstory/CAP-50/synthetic-v1.json`.
- Run instructor controls: `mvn -Dtest=CatalogInstructorHarnessTest test`.
- Run learner acceptance: `mvn -Plearner-tests -Dtest=CAP50AcceptanceTest test`.

Tests preserve distinction between processing-record ACCEPT and DUPLICATE, require exact editor attribution and immutable output, and reject invalid editor values. The import-service idempotency scenario remains a separately mapped contract; no transaction or service behavior is claimed by this bounded counter harness. Learner-owned method remains TODO. No timebox rehearsal has been performed.
