# Day 7: Create the first evaluation set

**Date:** 2026-11-02 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Fixed inputs, independently written labels, and evaluation cases

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Days 1–6; no model-generated labels.

## Task

Add labeled synthetic questions for supported, unsupported, and ambiguous clauses; labels are fixed independent of any model response.


Learner-owned scope: `ai/coverstory_assistant/learner_artifacts/day-07-labels.jsonl`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_07_fixture
```

Negative case: A label copied from candidate output is rejected; duplicate case IDs fail.

## Evidence

Fixture digest, case count, and label review record.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
