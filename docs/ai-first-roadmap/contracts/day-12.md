# Day 12: Compare a tiny local embedding proxy

**Date:** 2026-11-07 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Vectors and cosine similarity using a tiny fixed vocabulary

Read the exact source: [Retrieval | OpenAI API](https://developers.openai.com/api/docs/guides/retrieval) — **Retrieval**.


## Prerequisite

Day 11 lexical baseline; short worked vector example supplied.

## Task

Implement the bounded bag-of-terms vector comparison and report it beside the lexical baseline; label it a teaching proxy, not a production embedding model.


Learner-owned scope: `ai/coverstory_assistant/learner.py::tiny_vector_score`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_12
```

Negative case: Zero-vector, dimension mismatch, and tied similarity cases have fixed outcomes.

## Evidence

Both rankings on the same fixed corpus, with limitations stated.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
