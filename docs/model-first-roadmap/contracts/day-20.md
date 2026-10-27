# Day 20: Repeatable experiments

**Date:** 2026-11-15 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 7–19

## Learn

Seed, split, feature list and config are part of a reproducible result

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Write a compact experiment record and reproduce the same split/model output

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_20_experiment_record_preserves_seed_and_ordered_metadata`
**Acceptance:** Two fresh runs produce the same record fields and predictions
**Negative control:** Changing split seed without recording it fails the evidence check


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
