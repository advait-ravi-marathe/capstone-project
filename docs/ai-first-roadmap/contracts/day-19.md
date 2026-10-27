# Day 19: Keep retrieved instructions untrusted

**Date:** 2026-11-14 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Prompt injection boundary and source-data handling

Read the exact source: [Safety best practices | OpenAI API](https://developers.openai.com/api/docs/guides/safety-best-practices) — **Constrain user input and limit output tokens**.


## Prerequisite

Days 8 and 17; policy text remains quoted data.

## Task

Implement prompt assembly that separates fixed instructions from untrusted retrieved excerpts.


Learner-owned scope: `ai/coverstory_assistant/learner.py::assemble_grounded_input`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_19
```

Negative case: A hostile excerpt cannot replace the fixed instruction or cause a tool/action request.

## Evidence

Exact instruction/input separation and mutation rejection.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
