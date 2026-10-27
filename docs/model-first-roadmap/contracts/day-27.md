# Day 27: Trace the existing backend boundary

**Date:** 2026-11-22 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 26 trace

## Learn

Manual source diagnosis follows an existing policy issue request through owner lookup, idempotency and persistence

**Source:** [Repository source: existing local policy issue controller](../../../src/main/java/assignments/local/LocalPolicyIssueController.java)

## Learner-owned AI/model work

Read the existing controller call path, note one model-data error from earlier work, and write a concise evidence note without editing FE/BE

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_27_seeded_failure_analysis_uses_observed_prediction_errors`
**Acceptance:** Manual evidence records route, request/auth input, owner lookup, replay guard and persistence call
**Negative control:** Editing React/Spring/auth/controller code or calling this existing route an AI integration fails scope review

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- Which route handles a policy issue request? Where is the owner resolved, what idempotency lookup happens before persistence, and which component persists a new issue?

**Independent observation key**
- POST /api/policies receives PolicyIssueRequest and Authentication. LocalPolicyOwnerResolver resolves the owner; the controller looks up an existing issue receipt by owner and clientRequestId before asking the lifecycle service for an HTTP status. A new accepted issue is persisted atomically by PolicyLifecycleService. This is existing policy infrastructure, not an AI endpoint.


**Evidence category:** `manual_read_only_application_comprehension`

## Evidence

Manual source-comprehension note with exact file/line anchors, request or failure path, observed output and limits; automated helper test is separate; no FE/BE code-edit audit or completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
