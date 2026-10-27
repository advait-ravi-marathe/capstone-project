# Day 4: Vectors and dot products

**Date:** 2026-10-30 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 1–3

## Learn

Represent a feature row and weights as aligned vectors; bias is separate

## Worked teaching example

A dot product pairs same-position values and sums products: [0.5,2]·[3,2]=0.5*3+2*2=5.5. The vector lengths must match. Keep the bias separate from feature weights so a feature row of width n uses n weights after the bias.

**Source:** [Python Tutorial, 5.1.3. List Comprehensions](https://docs.python.org/3/tutorial/datastructures.html#list-comprehensions)

## Learner-owned AI/model work

Implement a dot product for two vectors and apply it to three fixed feature/weight pairs

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_04_dot_product`
**Acceptance:** Exact scalar scores match independent arithmetic
**Negative control:** Unequal vector lengths are rejected; this contract does not accept matrix rows


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
