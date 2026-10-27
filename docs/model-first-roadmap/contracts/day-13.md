# Day 13: Inspect errors by slice

**Date:** 2026-11-08 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 11–12

## Learn

Aggregate scores can hide minority or document-type failures

**Source:** [3.4. Metrics and scoring: quantifying the quality of predictions](https://scikit-learn.org/stable/modules/model_evaluation.html)

## Learner-owned AI/model work

Report metrics for two predeclared synthetic slices with denominators

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_13_slice_denominators_preserve_rows`
**Acceptance:** Every slice count and metric matches the independent expected table
**Negative control:** Empty/unknown slice and disappearing rows are rejected


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
