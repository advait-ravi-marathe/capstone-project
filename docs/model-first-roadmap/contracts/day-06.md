# Day 6: Batch gradient descent

**Date:** 2026-11-01 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 1–5

## Learn

Average per-example gradients before one update; learning rate controls step size

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Extend the one-step model to a small deterministic batch

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_06_batch_step_matches_independent_fixed_contract`
**Acceptance:** Independent expected gradient and post-step loss match fixed values
**Negative control:** Summing without averaging and reversing the sign are rejected


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
