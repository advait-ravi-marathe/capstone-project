# Day 26: Read the supplied request path

**Date:** 2026-11-21 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** comprehension_only

## Concept and source

Code comprehension: supplied React-to-Spring-to-AI request flow

Read the exact source: [Repository source](repo://src/main/java/assignments/local/LocalCoverStoryApplication.java;frontend/src/local/LocalCoverStoryApp.tsx) — **Assistant-supplied local request path**.


## Prerequisite

Day 1–25 AI component contracts; no FE/BE editing.

## Task

Read the assistant-owned UI/API code and annotate the request path. This is explicitly comprehension-only.


Learner-owned scope: `None; no learner FE/BE file; record evidence/ai-first/day-26.json`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_code_reading.Day26
```

Negative case: An annotation naming a learner-owned React/controller/auth edit is rejected as out of scope.

## Evidence

Short trace note and successful read-only probe; no implementation credit.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
