# Day 25: Account for usage without inventing measurements

**Date:** 2026-11-20 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Provider usage fields, bounded token/cost accounting

Read the exact source: [Text generation | OpenAI API](https://developers.openai.com/api/docs/guides/text) — **Choosing models and APIs; Message roles and instruction following**.


## Prerequisite

Day 3 adapter and Day 24 trace.

## Task

Implement a report that distinguishes provider-reported usage from unknown usage; never infer actual spend from a mock.


Learner-owned scope: `ai/coverstory_assistant/learner.py::usage_report`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_25
```

Negative case: Missing usage must remain unknown, not zero; negative or malformed counts fail.

## Evidence

Usage source and exact fields; live usage is unverified without an authorized call.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
