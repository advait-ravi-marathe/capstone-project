# Day 26: Trace the supplied AI request path

**Date:** 2026-11-21 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 24–25; supplied infrastructure only

## Learn

Manual read-only trace: UI event → supplied Spring boundary → Python fixture service

**Source:** [Repository source: supplied AI route and Python fixture service](../../../frontend/src/ai/AiFirstCoverStoryApp.tsx)

## Learner-owned AI/model work

Record exact files, request fields and outputs, then inspect one existing model error; no new model experiment is claimed

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_26_supplied_route_reading_is_paired_with_model_error_analysis`
**Acceptance:** Manual evidence names exact source lines and observed values
**Negative control:** Claiming learner-authored app code or a live model call is rejected

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- Which POST route sends the question? Does the browser choose the owner? Where does the server obtain caller identity?

**Independent observation key**
- The UI posts to /api/ai/policy-assistant with question and same-origin credentials plus CSRF. It sends no owner field in that request. Spring checks Authentication and forwards authentication.getName() as owner.


**Evidence category:** `manual_read_only_application_comprehension`

## Evidence

Manual source-comprehension note with exact file/line anchors, request or failure path, observed output and limits; automated helper test is separate; no FE/BE code-edit audit or completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
