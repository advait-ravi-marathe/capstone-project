# Day 22: Cross-validation without test tuning

**Date:** 2026-11-17 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 21 pipeline

## Learn

Cross-validation estimates variability on training data; final test stays untouched

**Source:** [3.1. Cross-validation: evaluating estimator performance](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Compare fold scores and retain one final test evaluation

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_22_preprocessing_fit_must_exclude_evaluation_ids`
**Acceptance:** Fold membership and score count match the fixed split plan
**Negative control:** Repeated test-driven hyperparameter selection is flagged


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
