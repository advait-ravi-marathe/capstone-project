# Day 23: Challenge the baseline with counterfactuals

**Date:** 2026-11-18 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Negative controls and mutation sensitivity

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Days 21–22.

## Task

Run fixed answer/citation counterfactuals and show which invariant each one violates.


Learner-owned scope: `ai/coverstory_assistant/learner.py::run_counterfactuals`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_23
```

Negative case: A changed claim with unchanged citation must be detected; a no-op control stays unchanged.

## Evidence

Before/after outputs and exact rejected-mutant list.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
