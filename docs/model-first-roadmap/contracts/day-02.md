# Day 2: Scores and probabilities

**Date:** 2026-10-28 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 1 Python lists and arithmetic

## Learn

A scalar score can be any real number; sigmoid maps it to (0,1)

## Worked teaching example

Worked example: with one feature x=2 and weights [bias=0, weight=1], score z=2. Sigmoid is 1/(1+e^-2)=0.880797; for score -2 it is 0.119203. The score is not itself a probability. Compute values from the inputs, preserve bias-first shape, and reject nonfinite values.

**Source:** [math — Mathematical functions: Power, exponential and logarithmic functions](https://docs.python.org/3/library/math.html#power-exponential-and-logarithmic-functions)

## Learner-owned AI/model work

Implement stable scalar sigmoid; predictor shape checks are a later task

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_02_sigmoid`
**Acceptance:** Check fixed scalar inputs -2, 0 and 2 against independent values
**Negative control:** Reject NaN and positive/negative infinity; width checks do not belong to this scalar function


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
