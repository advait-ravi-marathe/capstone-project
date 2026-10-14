# CAP-85 · Reconcile owned mock payment batches

**Status:** scaffold and instructor oracle prepared; learner aggregation query remains TODO. All records are synthetic. No provider calls, payment dispatch, or repair writes are part of this exercise.

## Contract

Implement `CAP85Seam.summarize(authenticatedOwnerId, batchId)` as one parameterized aggregate `SELECT`. Read only rows owned by the authenticated owner and requested batch. Return bigint counts in this order: `raw_count`, `invalid_count`, `duplicate_count`, `inserted_count`, `unaccounted_count`, and `missing_inserted_effect_count`.

Every staged row must have exactly one classification outcome. An `INSERTED` outcome must link to a durable payment effect for the same owner and exact staged row lineage. Return one all-zero summary for an empty owned batch. Do not infer confirmed money from these counts; confirmed captures and refunds have a separate reconciliation query.

The production seam is `src/main/java/coverstory/modules/payments/api/CAP85Seam.java`. PostgreSQL test schema is `src/test/resources/db/schema/cap85_batch.sql`; synthetic fixture is `fixtures/coverstory/CAP-85/synthetic-v1.json`.

## Acceptance cases

Run learner acceptance separately from the infrastructure suite:

```bash
mvn -q -Plearner-tests -Dtest=CAP85AcceptanceTest test
mvn -q -Dtest=CAP85InstructorInfrastructureTest test
```

The learner suite is expected to fail at the explicit TODO until the learner implements the seam. The instructor suite proves fixture loading, all five saved cases, tenant isolation, empty-batch behavior, and a deliberate defect that omits exact effect lineage.

Current local harness results and fixture/schema hashes are recorded in `docs/evidence/coverstory/CAP-85/harness-self-check.json`. It identifies the assignment files as uncommitted working-tree changes and does not claim learner completion.

Cases: four staged rows classify as two `INSERTED`, one `DUPLICATE`, one `INVALID`; a missing outcome is unaccounted; empty input yields a zero summary; 100 rows for owner B with the same batch ID do not affect owner A; and a missing effect remains visible even if classification counts balance.

## Evidence boundary

Instructor reference SQL lives only in `CAP85InstructorInfrastructureTest`; it is an oracle, not supplied learner implementation. Passing scaffold tests demonstrates that the local PostgreSQL harness and independent expected behavior work. It does not demonstrate that the learner TODO passes or that any live payment provider is integrated.
