# Day 25: Compare document-type baselines

**Date:** 2026-11-20 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 24 dataset

## Learn

A useful model is measured against a trivial baseline on held-out data

**Source:** [3.4. Metrics and scoring: quantifying the quality of predictions](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Compare majority, lexical and linear model errors on the same held-out fixture

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_25_error_ids_retain_order_and_ignore_correct_rows`
**Acceptance:** Independent confusion tables and selected error IDs match
**Negative control:** Changing test labels to improve the model is detected


**Evidence category:** `synthetic_document_model_and_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
