# CAP-43 — Classify sequential catalog import identity

**Day:** 002 · 15 Oct 2026 · **Phase:** 1 · **Role:** linked component · **Component:** CAP-43

This component shares Day 002's existing 95-minute practical budget with CAP-42; it adds no time. It supplies a bounded exact-key map policy for synthetic import rehearsal. It does not implement authentication, durable idempotency, concurrent safety, publication approval, or insurer acceptance.

## Learner task

Implement `classify(Map<ImportIdentityKey,String>, ImportIdentityCommand)` and `accept(...)` in [`src/main/java/coverstory/modules/catalog/api/CAP43Seam.java`](../../../src/main/java/coverstory/modules/catalog/api/CAP43Seam.java).

The exact key is `(editorFixture, sourceId, sourceVersion)`. A null/blank key field or null/blank hash yields `INVALID`. Classification is read-only. Missing identity is `ACCEPT`; same exact hash is `DUPLICATE`; different exact hash is `CONFLICT`. Hashes are opaque nonblank strings: do not trim them. `accept` may insert only for `ACCEPT` and must leave duplicate/conflict entries untouched. The existing learner suite covers all five saved cases and intentionally reaches the TODO.

## Catalog module boundary

The app's `CatalogImportIdentityService` calls the public Catalog-owned `CatalogImportIdentityPort`; the learner seam is the Spring bean that implements it. The module service has no map of its own and no persistence adapter, so it does not create hidden state or imply concurrent safety. A later authenticated, durable import workflow must still own transaction and permission rules. `CAP43AcceptanceTest` invokes this same use-case boundary; instructor witnesses use a test-only port implementation so setup tests pass without filling in the learner seam.

## Commands

```sh
mvn -Dtest=CAP43InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP43AcceptanceTest test
```

The first command checks the fixed synthetic fixture and correct/defective witness. The second is expected to stop at the learner TODO. Current setup evidence is in [`docs/evidence/coverstory/CAP-43/result.json`](../../evidence/coverstory/CAP-43/result.json). See the [CAP-42 primary guide](CAP-42.md) for Day 002's SQL control-total seam and shared budget.
