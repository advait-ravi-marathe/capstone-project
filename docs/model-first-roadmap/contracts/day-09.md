# Day 9: Scale using training data only

**Date:** 2026-11-04 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 7 split and Day 8 leakage

## Learn

Mean and scale are learned on train, then reused unchanged

**Source:** [12.1. Inconsistent preprocessing](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Fit a two-feature standardizer on train rows and transform distinct shifted held-out rows with those same statistics

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_09_standardizer_uses_training_statistics`
**Acceptance:** Train transforms have zero mean/unit scale and fixed held-out values use train-only statistics
**Negative control:** A transform refitted on held-out or combined rows is caught by shifted values; source inputs remain unchanged


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
