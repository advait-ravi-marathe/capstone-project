# Day 29: Run the integrated synthetic demonstration

**Date:** 2026-11-24 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** ai

## Concept and source

AI-only integration and reproducible synthetic evidence

Read the exact source: [Working with evals | OpenAI API](https://developers.openai.com/api/docs/guides/evals) — **Working with evals**.


## Prerequisite

Days 1–25 AI seams; Days 26–28 reading evidence.

## Task

Implement run_offline_pipeline to compose tenant filtering, retrieval, grounded prompt assembly, response parsing, claim-to-source validation, and abstention with a deterministic model fixture. The supplied loopback browser/API/Python service is labeled synthetic; no live call is made.


Learner-owned scope: `ai/coverstory_assistant/learner.py::run_offline_pipeline and integrated_report`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_29
```

Negative case: A foreign-tenant claim, unsupported answer, or no-evidence query must return a complete abstention; live-provider claims fail the evidence classification.

## Evidence

Exact integrated outputs, prompt sent to the fixed model fixture, source/version citations, and denied counterfactuals.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
