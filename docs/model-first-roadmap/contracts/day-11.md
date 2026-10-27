# Day 11: Confusion matrix

**Date:** 2026-11-06 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 10 baseline

## Learn

True/false positives/negatives answer different questions

**Source:** [3.4. Metrics and scoring: quantifying the quality of predictions](https://scikit-learn.org/stable/modules/model_evaluation.html)

## Learner-owned AI/model work

Build a confusion matrix from fixed labels and predictions

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_11_confusion_matrix_has_exact_counts`
**Acceptance:** Exact four cell counts match a hand-derived oracle
**Negative control:** Swapped positive class or omitted row is detected


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
