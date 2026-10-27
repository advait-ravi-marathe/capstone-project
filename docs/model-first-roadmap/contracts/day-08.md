# Day 8: Spot target leakage

**Date:** 2026-11-03 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 7 split

## Learn

A feature derived from the answer makes evaluation dishonest

**Source:** [12.2. Data leakage](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Fit a tiny threshold classifier on a synthetic clean feature and a separate target-copy feature; compare both on held-out rows and flag the forbidden column

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_08_reports_all_forbidden_features`
**Acceptance:** The clean-feature classifier and target-copy classifier have exact independently checked held-out predictions/errors; the leaky feature is explicitly disallowed
**Negative control:** Fitting on held-out labels, hiding the target-derived feature, or reporting the leaked score as valid fails


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
