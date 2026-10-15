# CAP-50 — Count accepted imports and preserve exact identity

**Day:** 006 · 19 Oct 2026 · **Phase:** 1 · **Role:** primary · **Timebox:** 150 minutes

CAP-50 asks for immutable counts of accepted import decisions per exact editor fixture. The saved learner seam remains `CAP50Seam.acceptedCounts`; the source plan's separate repository identity exercise is kept at the Catalog boundary and checked with a deliberately test-only fake.

## Learner task

Implement `src/main/java/coverstory/modules/catalog/api/CAP50Seam.java`: validate the full input before returning a result, count only `ACCEPT`, group by exact editor, include no zero buckets, and return an immutable map. Do not treat `DUPLICATE`, `CONFLICT`, or `INVALID` as accepted. The exact synthetic inputs and expected counts are in [`CAP-50 fixture`](../../../fixtures/coverstory/CAP-50/synthetic-v1.json).

The identity control separately demonstrates the sequential scenario: same `(editor, source)` and same hash replays as `DUPLICATE`; changed hash returns `CONFLICT`; neither replay nor conflict inserts another accepted record. This is a test oracle, **not** a production identity implementation or a durable/concurrent guarantee.

## Instructor setup and commands

```sh
mvn -Dtest=CatalogInstructorHarnessTest,CAP50InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP50AcceptanceTest test
```

The first command checks fixture shape and the test-only fake reference. The second runs the learner contract separately and is expected to stop at the explicit TODO until implemented. No timer rehearsal or mastery result is claimed.
