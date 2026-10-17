# Transition unit 08 · 2026-11-23 · CAP-66

**Saved contract:** [`day-024-cap-66.md`](../../../docs/assignments/coverstory/saved-days/day-024-cap-66.md) · **full cadence:** 180 minutes (150 practical + 30 AWS lesson/objective).

## Repository task

Retain consumer-group assignment, committed-next-offset and redelivery learning using bounded offline catalog refresh traces.

Work in `src/main/java/coverstory/modules/catalog/` and the task-specific source paths in the saved card. Use only the checked-in synthetic fixture. Preserve the learner TODO and do not modify instructor-oracle expected values. Produce a focused change plus one recorded adversarial result that traces to a saved acceptance case.

## Run instructor setup and learner acceptance separately

Instructor/setup command:

```sh
./mvnw -Dtest=CAP66InstructorInfrastructureTest test
```

Learner acceptance command:

```sh
./mvnw -Plearner-tests -Dtest=CAP66AcceptanceTest test
```

Harness files: instructor `src/test/java/coverstory/modules/catalog/CAP66InstructorInfrastructureTest.java`; learner `src/test/java/coverstory/modules/catalog/CAP66AcceptanceTest.java`. If the card is only mapped or the check is conditional, report that exact state and the dependency; a passing instructor test does not complete the learner task.

Use the remaining minutes from the source card for design, tests, explain-back, and evidence. Add 30 minutes for the saved AWS official-pathway lesson/objective. Record actual time and whether work was AI-assisted; do not claim certification or mastery from time spent or test status alone.
