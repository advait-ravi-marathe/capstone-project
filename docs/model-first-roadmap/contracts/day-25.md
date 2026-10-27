# Day 25: Compare document-type baselines

**Date:** 2026-11-20 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 24 dataset

## Learn

A useful model is measured against a trivial baseline on held-out data

**Source:** [3.4. Metrics and scoring: quantifying the quality of predictions](https://scikit-learn.org/stable/modules/model_evaluation.html)

## Learner-owned AI/model work

Train majority, centroid and one-step linear baselines using development rows; compare with a fixed lexical rule on the same held-out IDs/labels

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_25_error_ids_retain_order_and_ignore_correct_rows`
**Acceptance:** Each candidate has an independently checked confusion table, accuracy and ordered error IDs
**Negative control:** Omitting/changing a candidate, using held-out labels for fitting, or reordering IDs is rejected


**Evidence category:** `synthetic_document_model_and_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
