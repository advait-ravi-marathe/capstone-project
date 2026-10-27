# Day 30: Day 30 evaluated model slice

**Date:** 2026-11-25 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 24–29; synthetic dataset and saved linear model

## Learn

A compact model slice trains, saves, reloads and evaluates one synthetic document-type classifier

**Source:** [Repository source: model-building learner contract](../../../ai/model_building/learner.py)

## Learner-owned AI/model work

Run fixed training/evaluation examples with the model artifact; summarize held-out errors and limitations

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_30_model_scores_fixed_rows_in_declared_order`
**Acceptance:** Learner score function matches independent expected scores and preserves declared feature order
**Negative control:** A model artifact without held-out labels or a claim of customer/production value is rejected


**Evidence category:** `model_artifact_and_heldout_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
