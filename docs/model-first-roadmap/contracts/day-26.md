# Day 26: Trace the supplied AI request path

**Date:** 2026-11-21 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 24–25; supplied infrastructure only

## Learn

Read-only trace: UI event → supplied Spring boundary → Python model service

**Source:** [Repository source: supplied AI route and Python fixture service](../../../frontend/src/ai/AiFirstCoverStoryApp.tsx)

## Learner-owned AI/model work

Inspect provided code and record exact files, inputs and outputs; spend remaining time on a small model slice/error review

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_26_supplied_route_reading_is_paired_with_model_error_analysis`
**Acceptance:** Trace answers match source behavior and model metric counts match fixture
**Negative control:** Claiming learner-authored app code or a live model call is rejected

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- Which POST route sends the question? Does the browser choose the owner? Where does the server obtain caller identity?

**Independent observation key**
- The UI posts to /api/ai/policy-assistant with question and same-origin credentials plus CSRF. It sends no owner field in that request. Spring checks Authentication and forwards authentication.getName() as owner.


**Evidence category:** `read_only_application_comprehension_plus_model_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
