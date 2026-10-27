# Day 22: Cross-validation without test tuning

**Date:** 2026-11-17 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 21 pipeline

## Learn

Cross-validation estimates variability on training data; final test stays untouched

## Worked teaching example

Cross-validation scores describe repeated train/validation folds inside the development data. Keep fold IDs and individual scores, then summarize them; reserve the final test set for one separate check. Three fold accuracies .5, .5, 1.0 have mean 2/3, while a separate 2/3 final score is not another fold.

**Source:** [3.1. Cross-validation: evaluating estimator performance](https://scikit-learn.org/stable/modules/cross_validation.html)

## Learner-owned AI/model work

Construct three deterministic validation folds from six development IDs, calculate each fold score, then score two separate final-test IDs once

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_22_preprocessing_fit_must_exclude_evaluation_ids ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_22_retains_each_fold_score_and_one_final_test_result`
**Acceptance:** Exact fold membership, fold scores, mean, and separate final-test score match the independent expected report
**Negative control:** Duplicate IDs, missing/duplicated fold rows, or final-test IDs entering folds is rejected


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
