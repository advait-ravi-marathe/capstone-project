# Day 17: Vectorized gradient

**Date:** 2026-11-12 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 15–16

## Learn

Array reductions must preserve feature and batch axes

**Source:** [NumPy: the absolute basics for beginners](https://numpy.org/doc/stable/user/absolute_beginners.html)

## Learner-owned AI/model work

Implement a NumPy mean batch gradient and compare a non-square three-row, two-feature batch with an independent oracle

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_17_gradient_keeps_bias_and_feature_axes`
**Acceptance:** Bias and both feature gradients match expected values
**Negative control:** Summing rather than averaging rows, wrong axis, or dividing by feature count is rejected


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
