# Day 10: Split documents into stable chunks

**Date:** 2026-11-05 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Deterministic segmentation and boundary metadata

Read the exact source: [Retrieval | OpenAI API](https://developers.openai.com/api/docs/guides/retrieval) — **Retrieval**.


## Prerequisite

Day 9 normalized documents.

## Task

Implement a small deterministic sentence-boundary chunker for the supplied synthetic files; no vector database yet.


Learner-owned scope: `ai/coverstory_assistant/learner.py::chunk_document`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_10
```

Negative case: Empty chunks, changed text, and duplicate chunk IDs fail.

## Evidence

Exact chunk text/IDs and source section continuity.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
