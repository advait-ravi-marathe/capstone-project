# Day 24: Record a privacy-safe trace

**Date:** 2026-11-19 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Trace IDs, prompt versions, and content redaction

Read the exact source: [Safety best practices | OpenAI API](https://developers.openai.com/api/docs/guides/safety-best-practices) — **Constrain user input and limit output tokens**.


## Prerequisite

Days 8 and 21–23.

## Task

Implement a trace record that links fixture/model/prompt versions while excluding secrets and user/document text.


Learner-owned scope: `ai/coverstory_assistant/learner.py::make_safe_trace`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_24
```

Negative case: Trace containing API key, raw prompt, or policy text is rejected.

## Evidence

Trace schema, redaction assertions, and fixture-only label.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
