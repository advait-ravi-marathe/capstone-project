# Day 21: Combine the evaluation harness

**Date:** 2026-11-16 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Independent expected labels and actual pipeline outputs

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Days 7–20 completed locally.

## Task

Implement a deterministic evaluator over saved output records; it must not call a model or grade itself.


Learner-owned scope: `ai/coverstory_assistant/learner.py::evaluate_cases`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_21
```

Negative case: Echoing expected labels without reading actual output is caught by counterfactual cases.

## Evidence

Exact per-case decisions and independent totals.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
