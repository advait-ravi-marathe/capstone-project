# CAP-66 — Compare recorded consumer-group traces

**Status:** Instructor fixture and reference witnesses are runnable. Learner seam remains an explicit TODO; no assignment or Kafka integration readiness is claimed.

- Learner seam: `observe` in `src/main/java/coverstory/modules/catalog/api/CAP66Seam.java`.
- Synthetic trace: `fixtures/coverstory/CAP-66/synthetic-v1.json`.
- Learner contract: `src/test/java/coverstory/modules/catalog/CAP66AcceptanceTest.java`.
- Instructor fixture and defect witnesses: `src/test/java/coverstory/modules/catalog/CAP66InstructorInfrastructureTest.java`.

The saved card planned `src/main/java/capstone/coverstory/catalog/api/CAP66Seam.java` and `src/test/java/capstone/coverstory/catalog/CAP66Test.java`. This checkout's established Java package is `coverstory.modules.catalog`; the path difference is recorded here and does not change the saved behavioral contract.

The fixture is synthetic and offline. It describes three partitions and six publication versions; group G has two members with disjoint assignments, while independent group H observes the same six versions. A separate snapshot permits a fourth member to be idle. Restart traces show a committed offset of 41 resuming at 42 and a pre-commit restart that can redeliver the same record. The output may report redelivery and at-least-once possibility; it must never claim exactly-once delivery. Rebalances may change ownership, so the recorded assignments are snapshot facts only.

The instructor oracle has a positive witness for every saved case and negative witnesses for overlapping partition ownership, resuming at the committed offset instead of the next offset, and an unreported pre-commit repeat. These are offline fixture checks. They do not install Kafka, create a broker/topic, or produce runtime observations.

```sh
mvn -Dtest=CAP66InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP66AcceptanceTest test
```

The first command should pass. The second currently reaches the learner `UnsupportedOperationException` TODO. Passing the instructor checks only verifies fixture and oracle setup; measured learner effort and independent mastery remain unverified.
