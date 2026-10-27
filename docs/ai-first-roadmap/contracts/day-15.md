# Day 15: Check citation support

**Date:** 2026-11-10 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Citation integrity and claim-to-excerpt evidence

Read the exact source: [Retrieval | OpenAI API](https://developers.openai.com/api/docs/guides/retrieval) — **Retrieval**.


## Prerequisite

Day 14 citations.

## Task

Implement checks that every citation resolves to the exact supplied document version and section.


Learner-owned scope: `ai/coverstory_assistant/learner.py::validate_citations`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_15
```

Negative case: Valid citation plus unknown/foreign/duplicate extra citation must fail.

## Evidence

All citations checked; exact support and rejection record.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
