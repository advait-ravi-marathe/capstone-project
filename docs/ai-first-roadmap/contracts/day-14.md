# Day 14: Construct source citations

**Date:** 2026-11-09 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Bind each claim to a retrieved document and section

Read the exact source: [Retrieval | OpenAI API](https://developers.openai.com/api/docs/guides/retrieval) — **Retrieval**.


## Prerequisite

Days 9–13.

## Task

Implement citation construction from retrieved chunk IDs; do not invent document names or section labels.


Learner-owned scope: `ai/coverstory_assistant/learner.py::build_citations`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_14
```

Negative case: Unknown chunk ID and mismatched document version are rejected.

## Evidence

Exact citation rows and source-version digest.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
