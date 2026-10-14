# CAP-45 — Inspect import attempts without duplicating evidence

**Day:** 003 · 16 Oct 2026 · **Phase:** 1 · **Role:** linked component · **Component:** CAP-45

CAP-45 shares Day 003's existing 150-minute budget with CAP-44. It adds zero minutes. This is a synthetic catalog query rehearsal and makes no claim about production imports, payment eligibility, or provider behavior.

## Learner task

Implement the SQL at [`src/main/resources/coverstory/catalog/cap_45.sql`](../../../src/main/resources/coverstory/catalog/cap_45.sql). Return one row per published evidence identity for the requested editor and half-open evidence capture window. Join attempts on `(editor_fixture, source_id, source_version)` and aggregate attempts before joining so retries do not fan out amount totals. Preserve nullable amounts. Evidence without attempts remains present with count `0`, last attempt `NULL`, and `has_attempt=false`. Attempts may happen after the evidence capture window and still describe qualifying evidence.

## Instructor scaffolding

- Deterministic synthetic rows: `src/test/resources/coverstory/catalog/cap_45_schema.sql` and `cap_45_fixtures.sql`.
- Fixture summary: [`fixtures/coverstory/CAP-45/synthetic-v1.json`](../../../fixtures/coverstory/CAP-45/synthetic-v1.json).
- Test-only PostgreSQL adapter executes the saved learner query: `src/test/java/coverstory/modules/catalog/CAP45SqlPortAdapter.java`.
- Learner contracts: `src/test/java/coverstory/modules/catalog/CAP45SqlAcceptanceTest.java`.
- Independent instructor reference and mutant checks: `src/test/java/coverstory/modules/catalog/CAP45InstructorInfrastructureTest.java`.

Run the instructor oracle and learner contract separately:

```sh
mvn -Dtest=CAP45InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP45SqlAcceptanceTest test
```

The instructor reference distinguishes wrong-owner joins, inner joins that drop unmatched evidence, and raw one-to-many joins that duplicate amounts. Learner acceptance is expected to stop at the shape-only SQL TODO until implemented.
