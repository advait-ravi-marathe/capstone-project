# Day 18: Return a complete denial

**Date:** 2026-11-13 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Fail-closed result shape and non-disclosure

Read the exact source: [Safety best practices | OpenAI API](https://developers.openai.com/api/docs/guides/safety-best-practices) — **Constrain user input and limit output tokens**.


## Prerequisite

Day 17 authorization filter.

## Task

Implement a denial result with no answer, citations, or private metadata.


Learner-owned scope: `ai/coverstory_assistant/learner.py::denied_result`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_18
```

Negative case: Mutant with allowed=true or private citation must be rejected even if reason text is correct.

## Evidence

Full denial object and unchanged fixture state.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
