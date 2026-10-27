# Day 10: Majority and centroid baselines

**Date:** 2026-11-05 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 7–9

## Learn

A simple baseline can expose class imbalance and weak modeling

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Compare majority-class and nearest-centroid predictions on fixed labeled rows

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_10_majority_tie_breaks_to_zero`
**Acceptance:** Exact predictions and independent accuracy counts match
**Negative control:** A baseline that learns from held-out labels fails


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
