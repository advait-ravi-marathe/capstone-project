# Day 13: Measure retrieval errors

**Date:** 2026-11-08 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Recall-at-k and fixed relevant-document labels

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Days 7 and 11–12.

## Task

Implement a retrieval metric using independent relevant IDs and compare lexical versus tiny-vector rankings.


Learner-owned scope: `ai/coverstory_assistant/learner.py::recall_at_k`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_13
```

Negative case: Missing relevant IDs and a self-labeled candidate are rejected.

## Evidence

Exact numerator/denominator by method and fixed case IDs.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
