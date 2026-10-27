# Day 16: Abstain when evidence is insufficient

**Date:** 2026-11-11 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Decision thresholds from fixed evidence, not model confidence alone

Read the exact source: [Safety best practices | OpenAI API](https://developers.openai.com/api/docs/guides/safety-best-practices) — **Constrain user input and limit output tokens**.


## Prerequisite

Days 13–15.

## Task

Implement explicit supported/unsupported/ambiguous outcomes from independent retrieval and claim evidence.


Learner-owned scope: `ai/coverstory_assistant/learner.py::decide_abstention`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_16
```

Negative case: A confident model output without supporting evidence must abstain.

## Evidence

Decision table results for all three fixed labels.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
