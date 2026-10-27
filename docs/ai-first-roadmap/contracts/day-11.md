# Day 11: Build the lexical retrieval baseline

**Date:** 2026-11-06 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Term matching as a simple baseline before embeddings

Read the exact source: [Retrieval | OpenAI API](https://developers.openai.com/api/docs/guides/retrieval) — **Retrieval**.


## Prerequisite

Day 10 chunks; Python list/dict/Counter example supplied.

## Task

Implement exact deterministic lexical scoring against the tiny corpus.


Learner-owned scope: `ai/coverstory_assistant/learner.py::lexical_search`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_11
```

Negative case: Foreign-tenant and unrelated clauses must not win because they contain common words.

## Evidence

Fixed query ranking and explicit no-match case.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
