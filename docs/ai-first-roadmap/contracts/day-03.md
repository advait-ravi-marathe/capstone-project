# Day 3: Use the real provider adapter safely

**Date:** 2026-10-29 (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** integrated

## Concept and source

HTTP requests, environment configuration, and response boundaries

Read the exact source: [urllib.request — Extensible library for opening URLs](https://docs.python.org/3/library/urllib.request.html) — **urllib.request.urlopen**.


## Prerequisite

Days 1–2; instructor-owned adapter and local HTTP test server.

## Task

Use the supplied configurable Responses API adapter. The local HTTP fixture verifies request/response behavior. A live call is optional only after separate authorization and user-configured access.


Learner-owned scope: `ai/coverstory_assistant/provider.py (provided); learner evidence only`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
python3 -m unittest ai.coverstory_assistant.tests.test_provider_protocol
```

Negative case: Reject absent configuration and non-success/malformed responses without revealing the key; local server is not live-model evidence.

## Evidence

Local protocol test is infrastructure only; live-provider validation remains unverified until authorized.

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
