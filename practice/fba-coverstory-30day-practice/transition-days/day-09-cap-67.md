# Transition unit 09 · 2026-11-24 · CAP-67

**Saved contract:** [`day-025-cap-67.md`](../../../docs/assignments/coverstory/saved-days/day-025-cap-67.md) · **full cadence:** 180 minutes (150 practical + 30 AWS lesson/objective).

## Repository task

Retain recall, reciprocal rank and denominator integrity for synthetic product-wording evaluation.

Work in `ai/coverstory/cap_67.py` and the task-specific source paths in the saved card. Use only the checked-in synthetic fixture. Preserve the learner TODO and do not modify instructor-oracle expected values. Produce a focused change plus one recorded adversarial result that traces to a saved acceptance case.

## Run instructor setup and learner acceptance separately

Instructor/setup command:

```sh
python3 -m unittest ai.tests.test_coverstory_cap67_infrastructure
```

Learner acceptance command:

```sh
python3 -m unittest ai.coverstory.tests.test_cap_67
```

Harness files: instructor `ai/tests/test_coverstory_cap67_infrastructure.py`; learner `ai/coverstory/tests/test_cap_67.py`. If the card is only mapped or the check is conditional, report that exact state and the dependency; a passing instructor test does not complete the learner task.

Use the remaining minutes from the source card for design, tests, explain-back, and evidence. Add 30 minutes for the saved AWS official-pathway lesson/objective. Record actual time and whether work was AI-assisted; do not claim certification or mastery from time spent or test status alone.
