# Transition unit 02 · 2026-11-17 · CAP-60

**Saved contract:** [`day-017-cap-60.md`](../../../docs/assignments/coverstory/saved-days/day-017-cap-60.md) · **full cadence:** 180 minutes (150 practical + 30 AWS lesson/objective).

## Repository task

Use deterministic real-SQL races to prove idempotency, optimistic journey versions and atomic authoritative audit boundaries.

Work in `src/main/java/coverstory/modules/application/` and the task-specific source paths in the saved card. Use only the checked-in synthetic fixture. Preserve the learner TODO and do not modify instructor-oracle expected values. Produce a focused change plus one recorded adversarial result that traces to a saved acceptance case.

## Run instructor setup and learner acceptance separately

Instructor/setup command:

```sh
./mvnw -Dtest=CAP60InstructorInfrastructureTest test
```

Learner acceptance command:

```sh
./mvnw -Plearner-tests -Dtest=CAP60AcceptanceTest test
```

Harness files: instructor `src/test/java/coverstory/application/CAP60InstructorInfrastructureTest.java`; learner `src/test/java/coverstory/application/CAP60AcceptanceTest.java`. If the card is only mapped or the check is conditional, report that exact state and the dependency; a passing instructor test does not complete the learner task.

Use the remaining minutes from the source card for design, tests, explain-back, and evidence. Add 30 minutes for the saved AWS official-pathway lesson/objective. Record actual time and whether work was AI-assisted; do not claim certification or mastery from time spent or test status alone.
