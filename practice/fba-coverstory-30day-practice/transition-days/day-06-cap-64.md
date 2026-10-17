# Reference guide · CAP-64 (not a schedule entry)

**Reference only:** This guide is not part of the revised daily schedule. Use `transition-continuation.json` as the source-backed date/ID sequence.

**Saved contract:** [`day-022-cap-64.md`](../../../docs/assignments/coverstory/saved-days/day-022-cap-64.md) · **full cadence:** 180 minutes (150 practical + 30 AWS lesson/objective).

## Repository task

Preserve producer serialization, acknowledgment and bounded failure semantics as an offline port test.

Work in `src/main/java/coverstory/modules/catalog/` and the task-specific source paths in the saved card. Use only the checked-in synthetic fixture. Preserve the learner TODO and do not modify instructor-oracle expected values. Produce a focused change plus one recorded adversarial result that traces to a saved acceptance case.

## Run instructor setup and learner acceptance separately

Instructor/setup command:

```sh
./mvnw -Dtest=CAP64InstructorInfrastructureTest test
```

Learner acceptance command:

```sh
./mvnw -Plearner-tests -Dtest=CAP64AcceptanceTest test
```

Harness files: instructor `src/test/java/coverstory/modules/catalog/CAP64InstructorInfrastructureTest.java`; learner `src/test/java/coverstory/modules/catalog/CAP64AcceptanceTest.java`. If the card is only mapped or the check is conditional, report that exact state and the dependency; a passing instructor test does not complete the learner task.

Use the remaining minutes from the source card for design, tests, explain-back, and evidence. Add 30 minutes for the saved AWS official-pathway lesson/objective. Record actual time and whether work was AI-assisted; do not claim certification or mastery from time spent or test status alone.
