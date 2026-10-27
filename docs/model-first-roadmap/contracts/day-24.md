# Day 24: Build a synthetic document dataset

**Date:** 2026-11-19 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 7–23; no private data

## Learn

Labels describe document type, not truth, risk or customer eligibility

**Source:** [9.3. Generated datasets](https://scikit-learn.org/stable/common_pitfalls.html)

## Learner-owned AI/model work

Create the 12 declared synthetic schedule/exclusion/definition rows

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_24_document_fixture_is_fixed_and_split_safe`
**Acceptance:** Exact doc-01..doc-12 IDs, cyclic labels, 6/3/3 splits and fixed text match the independent contract
**Negative control:** Duplicate text/IDs across train, validation or test fail exact row comparison


**Evidence category:** `synthetic_document_model_and_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
