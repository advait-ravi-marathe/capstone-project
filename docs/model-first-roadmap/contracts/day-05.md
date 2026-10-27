# Day 5: Check a gradient numerically

**Date:** 2026-10-31 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 1–4

## Learn

A finite difference estimates how loss changes when one parameter moves

## Worked teaching example

For f(x)=x² at x=3, the central difference [f(3+ε)-f(3-ε)]/(2ε) approaches 6 as ε gets small. For logistic loss, perturb parameter j only; its analytic derivative is (p-y)*x_j. Two distinct feature coordinates ensure selecting the wrong parameter index changes the answer. Epsilon must be positive and comparison uses a stated tolerance.

**Source:** [Python Tutorial, 4.8. Defining Functions](https://docs.python.org/3/tutorial/controlflow.html#defining-functions)

## Learner-owned AI/model work

Compare a logistic binary-cross-entropy parameter derivative with a central finite-difference estimate

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_05_finite_difference`
**Acceptance:** Analytic derivative (sigmoid(2w)-1)*2 at w=.3 agrees within 1e-8
**Negative control:** Wrong sign, wrong feature parameter or nonpositive epsilon is detected


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
