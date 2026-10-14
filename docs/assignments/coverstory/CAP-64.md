# CAP-64 — Test catalog publication callback contract offline

**Saved day:** 022 · 4 November 2026 · **Primary ID:** CAP-64 · **Maximum allocation:** 150 minutes

## Purpose

Preserve producer serialization, acknowledgment, and bounded failure semantics behind an offline port. This is an instructor scaffold and a learner exercise. The fixture is synthetic, no Kafka client or runtime broker is used, and a fake acknowledgment is not broker evidence.

## Files

- Learner seam: `src/main/java/coverstory/modules/catalog/api/CAP64Seam.java`
- Learner acceptance: `src/test/java/coverstory/modules/catalog/CAP64AcceptanceTest.java`
- Instructor self-check: `src/test/java/coverstory/modules/catalog/CAP64InstructorInfrastructureTest.java`
- Synthetic contract: `fixtures/coverstory/CAP-64/synthetic-v1.json`

The learner owns only `CompletionStage<PublishResult> publish(CatalogPublication event)` and its focused behavior. Use the existing `SourceVersionKey` record from CAP-165 for source/version identity. Keep serialization schema version 1, topic label `cs.catalog.publication.v1`, and the fake callback as the completion boundary.

## Required behavior

1. Validate the catalog version UUID, required fields, and typed source/version key before sending. Invalid input yields `VALIDATION_ERROR` and sends zero records.
2. Serialize the exact envelope fields `schemaVersion`, `catalogVersionId`, `catalogRevision`, `evidenceStatus`, and `payloadHash`.
3. Complete success only from the supplied acknowledgment callback. The recorded fake acknowledgment is `SIMULATED_ACK`, partition 1, offset 7.
4. A callback that does not arrive within the configured bound returns `PUBLISH_TIMEOUT` with no partition or offset.
5. Guard the returned result against duplicate callback completion. This says nothing about global or physical exactly-once delivery.

Do not add Kafka dependencies, make a network call, claim PostgreSQL-to-broker atomicity, or implement the entire learner behavior in the scaffold.

## Checks

The instructor reference test is runnable independently of the TODO seam:

```sh
mvn -q -Dtest=CAP64InstructorInfrastructureTest test
```

The learner acceptance contract is expected to fail at the explicit TODO until implemented:

```sh
mvn -q -Plearner-tests -Dtest=CAP64AcceptanceTest test
```

Current evidence must be recorded as setup/reference results separately from learner acceptance outcomes. A passing instructor witness does not establish learner mastery or timebox fit.

## Saved acceptance mapping

| Case | Input | Expected result |
|---|---|---|
| CAP-64-CS-01 | Valid envelope and fake ack at partition 1, offset 7 | Exact schema and typed key; `SIMULATED_ACK` with 1/7 metadata |
| CAP-64-CS-02 | Callback withheld beyond configured bound | `PUBLISH_TIMEOUT`; no success metadata |
| CAP-64-CS-03 | Invalid UUID or missing version | `VALIDATION_ERROR`; producer send count remains zero |
| CAP-64-CS-04 | Same operation receives a duplicate callback | One completion of the returned result; no physical exactly-once claim |

## Scope and interpretation

The saved task is bounded to the producer serialization, typed key, callback, and timeout seam. Acknowledgment leaves the database/broker dual-write failure window open; durable publication recovery is a separate assignment. Synthetic fixture metadata must never be described as a real broker run. The saved 95-minute capstone envelope has not been rehearsed by this scaffold.
