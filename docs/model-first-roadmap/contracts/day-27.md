# Day 27: Diagnose a model-backed failure

**Date:** 2026-11-22 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 26 trace

## Learn

Manual source diagnosis separates model prediction errors from HTTP, serialization and UI transport failures

**Source:** [Repository source: supplied Spring AI controller and Python fixture service](../../../src/main/java/assignments/local/LocalAiAssistantController.java)

## Learner-owned AI/model work

Trace source-defined blank-question/unavailable-service paths and inspect an existing model data error; do not edit FE/BE

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_27_seeded_failure_analysis_uses_observed_prediction_errors`
**Acceptance:** Manual evidence records exact HTTP status, error code, safe UI message and source locations
**Negative control:** Editing React/Spring/auth/controller code or claiming a new model experiment fails scope review

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- What status/code does a blank question produce? What happens when the gateway reports unavailable? Which safe UI message is shown?

**Independent observation key**
- Blank question: HTTP 400 INVALID_QUESTION. Gateway unavailable: HTTP 503 AI_PROVIDER_UNAVAILABLE. The UI shows a safe local-service-unavailable message.


**Evidence category:** `manual_read_only_application_comprehension`

## Evidence

Manual source-comprehension note with exact file/line anchors, request or failure path, observed output and limits; automated helper test is separate; no FE/BE code-edit audit or completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
