# Day 27: Diagnose a model-backed failure

**Date:** 2026-11-22 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 26 trace

## Learn

Separate model prediction errors from HTTP, serialization and UI transport failures

**Source:** [Repository source: supplied Spring AI controller and Python fixture service](../../../src/main/java/assignments/local/LocalAiAssistantController.java)

## Learner-owned AI/model work

Trace source-defined blank-question and unavailable-service paths; then repair one model data/metric error

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_27_seeded_failure_analysis_uses_observed_prediction_errors`
**Acceptance:** Record exact HTTP status, error code and safe UI message, separately from model error IDs
**Negative control:** Editing React/Spring/auth/controller code is out of scope; guessing without source evidence fails

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- What status/code does a blank question produce? What happens when the gateway reports unavailable? Which safe UI message is shown?

**Independent observation key**
- Blank question: HTTP 400 INVALID_QUESTION. Gateway unavailable: HTTP 503 AI_PROVIDER_UNAVAILABLE. The UI shows a safe local-service-unavailable message.


**Evidence category:** `read_only_application_comprehension_plus_model_experiment`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
