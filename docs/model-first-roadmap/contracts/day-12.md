# Day 12: Precision and recall

**Date:** 2026-11-07 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 11 confusion matrix

## Learn

Threshold choices trade missed positives against false alarms

**Source:** [3.3. Tuning the decision threshold for class prediction](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Calculate precision/recall at two fixed thresholds and explain the change

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_12_undefined_metrics_remain_none`
**Acceptance:** Exact counts and fractions match independently enumerated predictions
**Negative control:** Undefined denominator must be explicit, not silently reported as 1.0


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
