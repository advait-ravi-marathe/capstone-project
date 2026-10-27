# Day 18: Numerical edge cases

**Date:** 2026-11-13 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 16 vectorized prediction

## Learn

Floating-point precision and extreme logits can cause overflow

**Source:** [NumPy: the absolute basics for beginners](https://numpy.org/doc/stable/user/absolute_beginners.html)

## Learner-owned AI/model work

Compare a stable sigmoid implementation over moderate and extreme scores

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_18_extreme_sigmoid_is_finite`
**Acceptance:** Outputs are finite and monotonic at fixed boundary values
**Negative control:** NaN, overflowed output or clipping that changes moderate values is caught


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
