# Day 1: A first trainable model

**Date:** 2026-10-27 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** None; instructor worked example is runnable first

## Learn

Python values, lists, loops, JSON, a score, sigmoid and one gradient step

## Worked teaching example

### Python and model vocabulary

A Python variable names a value; a list keeps an ordered sequence; `for` visits each row without changing its order. Each example is a pair `(features, label)`: the two feature values are numbers, and the label is exactly 0 or 1. `weights` is a bias-first list `[b, w1, w2]`, so a two-feature row has one fewer weight than the weight list. `json.dumps` converts ordinary lists/numbers into readable JSON; it does not train anything.

### Score and sigmoid

For features `x=[x1,x2]`, calculate the scalar score `z=b+w1*x1+w2*x2`. The score can be any real number. The sigmoid maps it to a probability: `p=1/(1+exp(-z))`. At `z=0`, `p=0.5`; sigmoid alone does not make a prediction trustworthy.

### Loss and gradient worked by hand

Use row A `(x=[1,0], y=1)` and row B `(x=[0,1], y=0)`, starting weights `[0,0,0]`. Both scores are 0 and probabilities are 0.5. Binary cross-entropy for one row is `-y*ln(p)-(1-y)*ln(1-p)`, so each row has loss `-ln(0.5)=0.693147`; the mean is also 0.693147.

For this loss, the per-row gradient is `(p-y)*[1,x1,x2]`; the leading 1 is the bias coordinate. Row A gives `[-0.5,-0.5,0]`; row B gives `[0.5,0,0.5]`. Average the two rows coordinate by coordinate to get `[0,-0.25,0.25]`. A gradient says which direction increases loss. Gradient descent subtracts `learning_rate * gradient`, so rate 0.2 gives new weights `[0,0.05,-0.05]`. The independently recomputed mean loss falls to 0.668460. Do not copy the instructor function into learner.py: derive and implement the batch step yourself.

A valid one-feature input is weights `[0,0]` with row `[1]`; a true shape error is weights `[0,0,0]` with row `[1]`. Labels must be binary, the batch nonempty, and the rate positive. The acceptance also changes initial weights/labels, so hard-coded example output is insufficient.

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Implement one batch logistic-regression step in learner.py using the two fixed synthetic rows

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_day_01`
**Acceptance:** Expected weights are [0.0, 0.05, -0.05], inputs remain unchanged, and independent BCE loss falls
**Negative control:** No-update, wrong-sign, no-average and invalid-shape candidates fail; bias plus one feature is valid


**Evidence category:** `model_training_and_gradient_work`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
