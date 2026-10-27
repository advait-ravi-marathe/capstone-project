# Day 8: Spot target leakage

**Date:** 2026-11-03 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 7 split

## Learn

A feature derived from the answer makes evaluation dishonest

**Source:** [12.2. Data leakage](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Add a deliberately leaky feature, measure the misleading result, then remove it

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_08_reports_all_forbidden_features`
**Acceptance:** The test reports the leaky column and the clean baseline separately
**Negative control:** A pipeline that fits preprocessing on test rows fails


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
