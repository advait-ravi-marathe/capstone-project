# Day 9: Normalize synthetic documents

**Date:** 2026-11-04 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Python strings, document IDs, and source metadata

Read the exact source: [Retrieval | OpenAI API](https://developers.openai.com/api/docs/guides/retrieval) — **Retrieval**.


## Prerequisite

Days 1–8 Python/JSON basics.

## Task

Implement conservative whitespace normalization while preserving document ID, section, owner, and source version.


Learner-owned scope: `ai/coverstory_assistant/learner.py::normalize_document`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_09
```

Negative case: Missing/duplicate IDs and metadata loss are rejected.

## Evidence

Before/after document bytes and field equality assertions.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
