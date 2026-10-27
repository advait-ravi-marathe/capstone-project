# Day 5: Validate a structured answer

**Date:** 2026-10-31 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

JSON object shape, types, and allowed fields

Read the exact source: [Structured model outputs | OpenAI API](https://developers.openai.com/api/docs/guides/structured-outputs) — **When to use Structured Outputs via function calling vs via text.format; Structured Outputs vs JSON mode**.


## Prerequisite

Day 4 extracted text.

## Task

Implement parsing and exact schema validation for answer, claims, citations, and abstained.


Learner-owned scope: `ai/coverstory_assistant/learner.py::parse_structured_answer`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_05
```

Negative case: Reject wrong types, unknown keys, duplicate citations, and invalid JSON.

## Evidence

Observed parsed object and independent malformed-input results.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
