# Day 20: Repeatable experiments

**Date:** 2026-11-15 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 7–19

## Learn

Seed, split, feature list and config are part of a reproducible result

## Worked teaching example

Repeat the same three-row CPU logistic update from zero weights with rate 0.1. The mean gradient gives weights [0.0166667, 0.0333333, 0], and the two declared held-out feature rows score 0.05 and 0.0166667, both class 1 under the stated zero-score cutoff. Save seed 41, ordered training IDs, feature order and predictions. A separate Python process recomputes these values; identical metadata alone would not prove the model computation repeated.

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Run the fixed train-only logistic step twice, record ordered IDs/features/predictions, and compare a fresh-process JSON result

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_20_experiment_record_preserves_seed_and_ordered_metadata`
**Acceptance:** Independent fresh process emits the same canonical record and predictions
**Negative control:** Changed order, omitted seed, or a nondeterministic model output fails


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
