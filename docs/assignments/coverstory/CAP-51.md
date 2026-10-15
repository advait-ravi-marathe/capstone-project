# CAP-51 — Aggregate import evidence without attempt fanout

**Day:** 006 · 19 Oct 2026 · **Phase:** 1 · **Role:** linked component · **Component:** CAP-51

CAP-51 shares CAP-50's 150-minute day and adds zero minutes. The SQL exercises synthetic evidence and import-attempt records; confirmation does not verify insurer evidence or price.

## Learner task

Implement the saved SQL at [`src/main/resources/coverstory/catalog/cap_51.sql`](../../../src/main/resources/coverstory/catalog/cap_51.sql). For one editor and a half-open UTC capture window, count published evidence rows, sum each known integer-minor-unit amount once, and count evidence identities with at least one successful attempt. Preserve `NULL` raw amounts; they count as rows but not as prices. Use the exact `(editor_fixture, source_id)` owner key.

## Instructor scaffolding

- Disposable schema and deterministic fixture: `src/test/resources/coverstory/catalog/cap_51_schema.sql` and `cap_51_fixtures.sql`.
- Fixture contract: [`fixtures/coverstory/CAP-51/synthetic-v1.json`](../../../fixtures/coverstory/CAP-51/synthetic-v1.json).
- Learner acceptance: `src/test/java/coverstory/modules/catalog/CAP51SqlAcceptanceTest.java`.
- Independent reference and known-bad controls: `src/test/java/coverstory/modules/catalog/CAP51InstructorInfrastructureTest.java`.

```sh
mvn -Dtest=CAP51InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP51SqlAcceptanceTest test
```

The instructor check runs against embedded PostgreSQL 14 and rejects raw one-to-many joins and cross-editor attempt joins. The learner query remains a typed empty-result TODO until implemented.
