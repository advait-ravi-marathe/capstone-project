# Day 19: Learning-rate comparison

**Date:** 2026-11-14 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 6 and 17

## Learn

On a simple convex loss, a step that is too large can oscillate and diverge; this is a teaching probe, not a universal model claim

**Source:** [Python Tutorial, 3. An Informal Introduction to Python](https://docs.python.org/3/tutorial/introduction.html)

## Learner-owned AI/model work

Run three fixed rates on f(w)=(w−2)^2 and record every loss checkpoint

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_19_curve_retains_every_rate_and_checkpoint`
**Acceptance:** The independent contract requires the small rate to reduce loss and the high rate to increase it
**Negative control:** Cherry-picking only the best endpoint or omitting the high-rate curve fails


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
