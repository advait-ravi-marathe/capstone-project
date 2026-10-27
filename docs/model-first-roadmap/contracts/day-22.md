# Day 22: Fold scoring without test tuning

**Date:** 2026-11-17 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 21 pipeline

## Learn

Cross-validation separates development folds from a final test; this bounded exercise scores fixed prediction fixtures and does not retrain a model per fold

## Worked teaching example

Partition six ordered development IDs into three round-robin folds: [a,d], [b,e], [c,f]. This exercise scores declared fixed predictions, yielding .5, .5, 1.0 and a mean of 2/3; it does not refit a model per fold. Two separate final-test IDs scored .5 are checked once and are not another fold. A later model evaluation may be called cross-validated only after it fits a fresh estimator inside each development fold while leaving the final test untouched.

**Source:** [3.1. Cross-validation: evaluating estimator performance](https://scikit-learn.org/stable/modules/cross_validation.html)

## Learner-owned AI/model work

Construct three deterministic validation folds from six development IDs, score fixed predictions per fold, then score two separate final-test IDs once

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_22_preprocessing_fit_must_exclude_evaluation_ids ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_22_retains_each_fold_score_and_one_final_test_result`
**Acceptance:** Exact fold membership, fixed-prediction scores, mean, and separate two-row final-test score match the independent expected report
**Negative control:** Duplicate IDs, missing/duplicated fold rows, or final-test IDs entering folds is rejected


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
