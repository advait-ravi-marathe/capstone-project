# CAP-93 — Probe local durable work during outage

## Learner seam and paths

Implement `CAP93Seam.runOutageProbe` in [CAP93Seam.java](../../../src/main/java/coverstory/modules/payments/api/CAP93Seam.java). Synthetic inputs are in [synthetic-v1.json](../../../fixtures/coverstory/CAP-93/synthetic-v1.json), the learner contract is [CAP93AcceptanceTest.java](../../../src/test/java/coverstory/modules/payments/CAP93AcceptanceTest.java), and independent local-oracle checks are [CAP93InstructorInfrastructureTest.java](../../../src/test/java/coverstory/modules/payments/CAP93InstructorInfrastructureTest.java).

Run instructor setup checks and learner acceptance separately:

```sh
mvn -Dtest=CAP93InstructorInfrastructureTest test
mvn -Dtest=CAP93AcceptanceTest -DexcludeLearnerAcceptance= test
```

The second command is expected to fail at the explicit TODO until the learner implements the report seam. The instructor checks only prove that the supplied synthetic model and its expected outputs are internally consistent.

## Contract

The test-only virtual clock accepts three synthetic read/reconciliation jobs while an outage lasts from 0 through 30 simulated seconds. At outage end, all three remain pending and none is complete. After recovery, the bounded local oracle completes each unique job once. Repeated delivery of an already completed job is inert. These jobs only reconcile existing references; they create no business effect and make no payment attempt.

The report also includes the supplied diagnostic category counts: 2 correct, 1 partial, 3 incorrect, and 2 not attempted, totaling 8/12. Label this a diagnostic count, never an AWS exam score. Without a benchmark or target job description, capacity and role fit remain `UNRESOLVED`.

This is a deterministic local simulation. It does not start a broker, call a provider, change payment authority, or establish behavior of a production PostgreSQL outbox/worker. No integrated probe output is supplied, so actual integrated execution remains `BLOCKED`; its evidence remains `NOT_RUN`. A diagram or test-only oracle is not a substitute for observed output.

## Learner ownership and limits

Learners implement the bounded report and its assertions. They do not add Kafka, SQS, Redis, a runtime queue, a payment attempt, or provider calls. Do not claim capacity, role fit, delivery guarantees beyond this fixture, or independent mastery based on infrastructure checks passing.
