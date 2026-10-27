# Day 1: A first trainable model

**Date:** 2026-10-27 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** None; instructor worked example is runnable first

## Learn

Python values, lists, loops, JSON, a score, sigmoid and one gradient step

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Implement one batch logistic-regression step in learner.py using the two fixed synthetic rows

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_day_01`
**Acceptance:** Expected weights are [0.0, 0.05, -0.05], inputs remain unchanged, and independent BCE loss falls
**Negative control:** No-update and reversed-gradient candidates must fail the exact weight/loss checks


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
