# Day 4: Extract the response text

**Date:** 2026-10-30 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Read nested JSON objects and distinguish text from other output items

Read the exact source: [Text generation | OpenAI API](https://developers.openai.com/api/docs/guides/text) — **Choosing models and APIs; Message roles and instruction following**.


## Prerequisite

Day 3 adapter response fixture.

## Task

Implement extraction of completed message output text from a fixed Responses-shaped payload.


Learner-owned scope: `ai/coverstory_assistant/learner.py::extract_response_text`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_04
```

Negative case: Refusal, incomplete, empty, or tool-only payloads must not be treated as a final answer.

## Evidence

Exact extracted text and rejected payload cases; fixture evidence.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
