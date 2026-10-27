# Day 5: Check a gradient numerically

**Date:** 2026-10-31 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 1–4

## Learn

A finite difference estimates how loss changes when one parameter moves

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Compare one analytic parameter gradient with a central finite-difference estimate

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_05_finite_difference`
**Acceptance:** Absolute difference is below a stated tolerance on fixed synthetic data
**Negative control:** Wrong sign and wrong parameter index are detected


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
