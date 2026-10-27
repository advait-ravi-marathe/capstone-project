# Day 22: Report exact metrics

**Date:** 2026-11-17 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Confusion counts, citation accuracy, abstention, and denominators

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Day 21 evaluator.

## Task

Implement fixed metric counts from per-case outcomes, retaining denominators and undefined cases.


Learner-owned scope: `ai/coverstory_assistant/learner.py::summarize_metrics`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_22
```

Negative case: Wrong denominator, omitted failure, or score above 100 percent is rejected.

## Evidence

Raw confusion table and independently recomputed ratios.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
