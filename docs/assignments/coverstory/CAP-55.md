# CAP-55 — Validate an offline partition trace

**Learner outcome:** Retain Kafka key-locality and partition-order reasoning as an offline contract experiment; do not install a broker.

- Learner seam: `validatePartitionTrace` in `src/main/java/coverstory/modules/catalog/api/CAP55Seam.java`.
- Synthetic trace vectors: `fixtures/coverstory/CAP-55/synthetic-v1.json`.
- Learner acceptance suite: `src/test/java/coverstory/modules/catalog/CAP55AcceptanceTest.java`.
- Instructor trace and defect discrimination: `src/test/java/coverstory/modules/catalog/CAP55InstructorInfrastructureTest.java`.

The fixture records source-A sequences 1–3 at partition 0, offsets 10–12, and source-B at the same partition/offset 13. The correct witness accepts this trace: distinct keys may share a partition. Negative witnesses reorder one key's offsets, split that key across partitions, omit provenance metadata, use a blank key, or claim synthetic offsets came from a real broker. No test creates a producer, broker, topic, or cloud resource.

The Java signature and package are reconciled to this checkout's existing module layout. `ProbeReport` is only a validation result for supplied evidence; it does not produce offsets or claim live Kafka behavior. The learner method remains TODO.

```sh
mvn -Dtest=CAP55InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP55AcceptanceTest test
```

Instructor self-check should pass; the learner contract currently stops at `CAP55Seam`'s TODO. This is not a Kafka integration test or an assignment-readiness claim.
