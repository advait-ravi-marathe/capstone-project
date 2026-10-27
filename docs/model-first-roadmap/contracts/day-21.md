# Day 21: A scikit-learn pipeline baseline

**Date:** 2026-11-16 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Train/test discipline and basic probability

## Learn

A Pipeline keeps preprocessing and estimator fit behavior together

**Source:** [8.1. Pipelines and composite estimators](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Fit a logistic baseline through a preprocessing Pipeline on the training partition

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_21_baseline_against_same_heldout_labels`
**Acceptance:** Predictions and metric counts match a fixed independent control envelope
**Negative control:** Preprocessing fit on held-out rows is rejected


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
