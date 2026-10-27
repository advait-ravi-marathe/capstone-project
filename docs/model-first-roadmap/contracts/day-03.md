# Day 3: Binary cross-entropy

**Date:** 2026-10-29 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 2 scores and labels

## Learn

Log loss penalizes confident wrong predictions; labels are 0/1

## Worked teaching example

For probabilities [0.8, 0.25] and labels [1,0], per-row losses are -ln(0.8)=0.223144 and -ln(0.75)=0.287682; mean loss is 0.255413. A confident wrong prediction is penalized more. Clip only if the contract explicitly defines clipping; reject invalid probabilities and misaligned batches.

**Source:** [math — Mathematical functions: Power, exponential and logarithmic functions](https://docs.python.org/3/library/math.html#power-exponential-and-logarithmic-functions)

## Learner-owned AI/model work

Implement mean binary cross-entropy for a batch of probabilities and labels

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_03_binary_cross_entropy`
**Acceptance:** Match fixed hand-calculated two-row loss and show it changes with confidence
**Negative control:** Reject empty batches, invalid labels, and probabilities outside (0,1)


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
