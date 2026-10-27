# Day 6: Classify provider failures

**Date:** 2026-11-01 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Timeout, status, malformed output, and bounded retry policy

Read the exact source: [Text generation | OpenAI API](https://developers.openai.com/api/docs/guides/text) — **Choosing models and APIs; Message roles and instruction following**.


## Prerequisite

Day 3 protocol adapter; no live call required.

## Task

Map deterministic local adapter failures into safe error classes; never retry authorization or schema errors.


Learner-owned scope: `ai/coverstory_assistant/learner.py::classify_provider_failure`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_06
```

Negative case: A 401, malformed JSON, and timeout must not disclose response body or loop indefinitely.

## Evidence

Error classification and retry-count trace from a local fixture.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
