# Day 30: Review the Day 30 evidence gate

**Date:** 2026-11-25 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

Evidence categories, limitations, and unresolved live validation

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Day 29 integrated synthetic demonstration.

## Task

Generate a local evidence summary that separates code/tests, understanding, offline fixtures, and independently verified live-provider evidence.


Learner-owned scope: `ai/coverstory_assistant/learner.py::closeout_report`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_30
```

Negative case: Mocks cannot satisfy the live-provider field; missing evidence stays missing rather than becoming pass/fail completion.

## Evidence

Exact summary and open gates; no learner completion is prefilled.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.

## Supplied offline vertical slice

```sh
scripts/package-local-demo.sh
```
```sh
scripts/local-demo.sh
```

Open the printed app origin and append `/#/ai-first/coverstory` (the fragment stays in the browser). The launcher starts disposable local PostgreSQL and a loopback-only synthetic fixture service; it sends no provider request and records no learner evidence.
