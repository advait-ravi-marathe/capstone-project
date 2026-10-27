# Day 20: Evaluate document-quality failures

**Date:** 2026-11-15 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Language, stale source, OCR-like noise, and ambiguous text slices

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Days 7 and 9–19.

## Task

Add a small independently labeled quality slice and report its outcomes separately.


Learner-owned scope: `ai/coverstory_assistant/learner_artifacts/day-20-quality-labels.jsonl`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_20_quality_slice
```

Negative case: Dropping a failing quality case or relabeling from model output must fail.

## Evidence

Per-slice counts, fixture digest, and unresolved cases.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
