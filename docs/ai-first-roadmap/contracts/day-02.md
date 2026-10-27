# Day 2: Serialize the provider request

**Date:** 2026-10-28 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Python types, JSON serialization, and request boundaries

Read the exact source: [json — JSON encoder and decoder](https://docs.python.org/3/library/json.html) — **Basic Usage**.


## Prerequisite

Day 1 prompt object.

## Task

Implement deterministic JSON-safe request construction with an explicit model placeholder and bounded output-token field.


Learner-owned scope: `ai/coverstory_assistant/learner.py::build_responses_request`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_02
```

Negative case: Reject missing model, unknown fields, and a prompt that exceeds the fixed fixture limit.

## Evidence

Exact serialized bytes and negative-case results; no provider evidence.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
