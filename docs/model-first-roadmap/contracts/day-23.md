# Day 23: Uncertainty and calibration basics

**Date:** 2026-11-18 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 11–13 and probability basics

## Learn

A score near 0.5 is not proof of calibrated probability

**Source:** [1.16. Probability calibration](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Bin a fixed set of probabilities and report counts plus mean confidence/accuracy

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_23_calibration_bins_match_fixed_counts`
**Acceptance:** Independent bin counts and averages match
**Negative control:** Empty bins and rounding that changes assignment are detected


**Evidence category:** `numerical_array_and_ml_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
