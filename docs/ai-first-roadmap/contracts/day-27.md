# Day 27: Trace a synthetic end-to-end request

**Date:** 2026-11-22 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** comprehension_only

## Concept and source

Code comprehension: input, authorization, retrieval, provider adapter, response

Read the exact source: [Repository source](repo://src/main/java/assignments/local/LocalCoverStoryApplication.java;frontend/src/local/LocalCoverStoryApp.tsx) — **Assistant-supplied local request path**.


## Prerequisite

Day 26 trace note; no FE/BE edits.

## Task

Run the supplied offline fixture flow and trace request/response ownership across layers. This is explicitly comprehension-only.


Learner-owned scope: `None; no learner FE/BE file; record evidence/ai-first/day-27.json`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_code_reading.Day27
```

Negative case: A fixture or mock result must not be described as a live provider answer.

## Evidence

Read-only trace output and honest fixture/live classification.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
