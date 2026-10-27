# Day 8: Version one prompt change

**Date:** 2026-11-03 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Prompt versioning and controlled comparisons

Read the exact source: [Prompt engineering | OpenAI API](https://developers.openai.com/api/docs/guides/prompt-engineering) — **Prompt engineering**.


## Prerequisite

Day 7 fixed labels.

## Task

Implement a versioned prompt builder and compare two fixed prompt versions on the same cases.


Learner-owned scope: `ai/coverstory_assistant/learner.py::prompt_for_version`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_08
```

Negative case: Unknown versions and a changed fixture digest fail closed.

## Evidence

Two version IDs, same-case comparison output, and digest.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
