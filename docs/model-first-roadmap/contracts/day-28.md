# Day 28: Explain model output limits

**Date:** 2026-11-23 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 27 diagnosis

## Learn

A document-type label is not an answer, eligibility decision, or calibrated confidence

**Source:** [Repository source: supplied model fixture and application boundary](../../../ai/coverstory_assistant)

## Learner-owned AI/model work

Read a supplied prediction report, identify unsupported interpretation, and test a fixed counterexample

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_28_report_does_not_promote_score_to_decision`
**Acceptance:** Answer distinguishes class score, label, and downstream decision with exact fixture evidence
**Negative control:** Treating a toy score as customer truth or changing UI/backend code fails

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- What do executionMode, providerRequestMade and liveQualityEvaluated say in the fixture? Is a document type score an insurance decision?

**Independent observation key**
- offline_synthetic_fixture, false, false; no. The fixture is not model output, insurance advice or a coverage decision.


**Evidence category:** `read_only_application_comprehension_plus_model_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
