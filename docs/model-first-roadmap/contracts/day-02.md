# Day 2: Scores and probabilities

**Date:** 2026-10-28 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 1 Python lists and arithmetic

## Learn

A linear score is not a probability; sigmoid maps it to (0,1)

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Implement stable sigmoid and prediction for the supplied bias-first weight format

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_02_sigmoid`
**Acceptance:** Check fixed scores -2, 0 and 2 against independently calculated values
**Negative control:** Reject NaN, infinity and wrong feature width


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
