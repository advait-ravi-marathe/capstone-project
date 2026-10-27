# Day 14: Improve a linear baseline

**Date:** 2026-11-09 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 1–13

## Learn

Change one factor at a time and preserve a clean held-out comparison

**Source:** [3.4. Metrics and scoring: quantifying the quality of predictions](https://scikit-learn.org/stable/modules/model_evaluation.html)

## Learner-owned AI/model work

Compare the Day 1 model with a fixed-feature baseline; write a short error note

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_14_comparison_reports_source_ordered_error_ids`
**Acceptance:** Result table includes exact split, baseline, metric and error examples
**Negative control:** Selecting a model using test results is flagged as leakage


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
