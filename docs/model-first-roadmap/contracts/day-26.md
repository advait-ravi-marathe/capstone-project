# Day 26: Trace the supplied application shell

**Date:** 2026-11-21 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 24–25 model errors

## Learn

Manual read-only trace: inspect the existing local UI shell and distinguish rendered content from unwired behavior

**Source:** [Repository source: existing local CoverStory UI shell](../../../frontend/src/local/LocalCoverStoryApp.tsx)

## Learner-owned AI/model work

Record exact rendered text and identify that request/model behavior is not present; pair with one previously introduced model error analysis

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_26_supplied_route_reading_is_paired_with_model_error_analysis`
**Acceptance:** Manual evidence names exact source lines and separates existing behavior from missing integration
**Negative control:** Claiming an API call or learner-authored FE/BE code where none exists is rejected

## Read-only application comprehension

Answer from source; do not edit FE/BE files.

**Questions**
- What does the existing local CoverStory screen render? Which UI or request behaviors are explicitly still unwired?

**Independent observation key**
- It renders the local training heading and says policy issue/renewal preview will appear after the local API is wired. It contains no current question submission, model request or ownership selection; those are absent, not implied.


**Evidence category:** `manual_read_only_application_comprehension`

## Evidence

Manual source-comprehension note with exact file/line anchors, request or failure path, observed output and limits; automated helper test is separate; no FE/BE code-edit audit or completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
