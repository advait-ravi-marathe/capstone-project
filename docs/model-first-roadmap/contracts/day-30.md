# Day 30: Day 30 evaluated model slice

**Date:** 2026-11-25 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 24–29; fixed synthetic 3-feature dataset

## Learn

A compact model slice trains, saves, reloads and evaluates a three-class synthetic document-type centroid classifier

## Worked teaching example

The six training rows form three two-row groups. For each class, average its three feature counts to obtain a centroid; feature order is schedule_count, exclusion_count, definition_count. The three held-out rows are near their matching centroids. Train only on the six train IDs, serialize class/feature order and centroids, reload, then score the held-out IDs and count errors. The tiny fixed synthetic dataset is an implementation exercise, not evidence of generalization to real policy documents.

**Source:** [Repository source: model-building learner contract](../../../ai/model_building/learner.py)

## Learner-owned AI/model work

Fit class centroids on six declared training rows, serialize/reload the model, classify three separate held-out rows and report exact errors

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_30_train_save_reload_and_evaluate_document_type_model`
**Acceptance:** Class order, feature order, training IDs, centroids, held-out predictions and error count match independent expected values
**Negative control:** Training on held-out IDs, swapping features/classes, malformed artifact data, or claiming customer/production value is rejected


**Evidence category:** `model_artifact_and_heldout_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
