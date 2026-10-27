# Day 16: Vectorized prediction

**Date:** 2026-11-11 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 15 shapes

## Learn

Broadcasting applies the same linear operation across rows

**Source:** [NumPy: the absolute basics for beginners](https://numpy.org/doc/stable/user/absolute_beginners.html)

## Learner-owned AI/model work

Implement vectorized scores and compare them row-by-row to Day 4 scalar scores

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_16_batched_scores_match_scalar_oracle`
**Acceptance:** All scores match scalar oracle within 1e-12
**Negative control:** Wrong axis/bias broadcasting fails a non-square fixture


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
