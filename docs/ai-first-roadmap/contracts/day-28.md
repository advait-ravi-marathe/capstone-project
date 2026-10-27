# Day 28: Diagnose one supplied cross-layer failure

**Date:** 2026-11-23 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** comprehension_only

## Concept and source

Code comprehension: locate failure without modifying FE/BE

Read the exact source: [Repository source](repo://src/main/java/assignments/local/LocalCoverStoryApplication.java;frontend/src/local/LocalCoverStoryApp.tsx) — **Assistant-supplied local request path**.


## Prerequisite

Day 27 flow; failure is seeded in an instructor fixture.

## Task

Inspect the supplied failure and identify the failing AI boundary; do not change Java, TypeScript, UI, controller, or auth tests. This is explicitly comprehension-only.


Learner-owned scope: `None; no learner FE/BE file; record evidence/ai-first/day-28.json`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_code_reading.Day28
```

Negative case: Changing a frontend/controller/auth file is outside the unit; blaming a healthy layer fails the diagnosis rubric.

## Evidence

Diagnosis record with observed output and code location.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
