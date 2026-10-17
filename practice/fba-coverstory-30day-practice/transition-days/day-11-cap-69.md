# Reference guide · CAP-69 (not a schedule entry)

**Reference only:** This guide is not part of the revised daily schedule. Use `transition-continuation.json` as the source-backed date/ID sequence.

**Saved contract:** [`day-027-cap-69.md`](../../../docs/assignments/coverstory/saved-days/day-027-cap-69.md) · **full cadence:** 180 minutes (150 practical + 30 AWS lesson/objective).

## Repository task

Use a deterministic catalog-export burst trace to compare direct rejection and bounded in-process queueing.

Work in `ai/coverstory/cap_69.py` and the task-specific source paths in the saved card. Use only the checked-in synthetic fixture. Preserve the learner TODO and do not modify instructor-oracle expected values. Produce a focused change plus one recorded adversarial result that traces to a saved acceptance case.

## Run instructor setup and learner acceptance separately

Instructor/setup command:

```sh
python3 -m unittest ai.tests.test_coverstory_cap69_infrastructure
```

Learner acceptance command:

```sh
python3 -m unittest ai.coverstory.tests.test_cap_69
```

Harness files: instructor `ai/tests/test_coverstory_cap69_infrastructure.py`; learner `ai/coverstory/tests/test_cap_69.py`. If the card is only mapped or the check is conditional, report that exact state and the dependency; a passing instructor test does not complete the learner task.

Use the remaining minutes from the source card for design, tests, explain-back, and evidence. Add 30 minutes for the saved AWS official-pathway lesson/objective. Record actual time and whether work was AI-assisted; do not claim certification or mastery from time spent or test status alone.
