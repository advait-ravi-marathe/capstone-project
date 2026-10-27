# Day 1: Build a grounded policy prompt

**Date:** 2026-10-27 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Python dictionaries, JSON values, and a bounded prompt

Read the exact source: [Text generation | OpenAI API](https://developers.openai.com/api/docs/guides/text) — **Choosing models and APIs; Message roles and instruction following**.

Supplemental: [json — JSON encoder and decoder](https://docs.python.org/3/library/json.html) — **Basic Usage**.

## Prerequisite

None; begin with the worked example.

## Task

Implement the exact instructions/input object for one synthetic hail-coverage question. Do not call a model.

Instructor-owned setup command:

```sh
python3 -m ai.coverstory_assistant.day01_example
```


Learner-owned scope: `ai/coverstory_assistant/learner.py::build_policy_prompt`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_01
```

Negative case: Blank question or excerpt must raise ValueError; extra prompt fields fail.

## Evidence

Command output plus the exact request object; fixture-only evidence.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
