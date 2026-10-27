# Day 17: Filter retrieval by tenant

**Date:** 2026-11-12 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Authorization before ranking or answer generation

Read the exact source: [Safety best practices | OpenAI API](https://developers.openai.com/api/docs/guides/safety-best-practices) — **Constrain user input and limit output tokens**.


## Prerequisite

Day 9 source ownership fields; Day 16 decision table.

## Task

Implement pre-retrieval tenant filtering for synthetic user/document memberships.


Learner-owned scope: `ai/coverstory_assistant/learner.py::filter_authorized_chunks`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_17
```

Negative case: Foreign-owner clauses must not reach the model prompt or citations.

## Evidence

Allowed and denied IDs plus prompt-input disclosure assertion.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
