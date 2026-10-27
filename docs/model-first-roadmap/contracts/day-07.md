# Day 7: Keep a held-out set

**Date:** 2026-11-02 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Python collections and Day 6 batch model

## Learn

Train, validation and test answer different questions; split before fitting

**Source:** [3.1. Cross-validation: evaluating estimator performance](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Create a deterministic split manifest for synthetic document labels

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_07_split_is_disjoint_and_ordered`
**Acceptance:** Exact record IDs are disjoint and class counts match the independent manifest
**Negative control:** Duplicate IDs and a leaked test row fail


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
