# Day 28: Explain model output limits

**Date:** 2026-11-23 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 27 diagnosis

## Learn

Manual review: a document-type label is not an answer, eligibility decision, or calibrated confidence

**Source:** [Repository source: fixed model-output fixture and learner report contract](../../../ai/model_building/fixtures/day-28-example.json)

## Learner-owned AI/model work

Read a fixed synthetic model-output example and state its limits using the counterexample; write a concise evidence note, with no FE/BE edits

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_28_report_does_not_promote_score_to_decision`
**Acceptance:** Manual note distinguishes score, label and downstream decision with exact fixture evidence
**Negative control:** Treating a toy score as customer truth or changing UI/backend code fails

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- In the fixed synthetic report, what do score, predicted_label and downstream_decision mean? Does the score establish a claim decision or confidence calibration?

**Independent observation key**
- The example reports score 0.92, predicted label 1 and downstream_decision not_assessed. It is a synthetic model-output example only; it establishes neither an insurance decision nor calibrated confidence.


**Evidence category:** `manual_read_only_application_comprehension`

## Evidence

Manual source-comprehension note with exact file/line anchors, request or failure path, observed output and limits; automated helper test is separate; no FE/BE code-edit audit or completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
