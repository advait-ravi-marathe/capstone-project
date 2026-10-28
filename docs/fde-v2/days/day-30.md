# Actual learning Day 30: Role-fit review and bounded security authoring

**Status: Not started.** Planned date: **2026-11-25** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; use the explicit roadmap position and actual attempt date.
- Role interview/AWS bank: 30 minutes.
- AI work: 90 minutes total, split below. No extra visibility block is scheduled.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours.

## Role practice (30 minutes)

Topic: **Role-fit and progress review**. Review actual interview evidence, permitted application preparation, AI carryover and next-month priorities; no automatic outreach. Use [the role bank and scoring rubric](../role-bank.md). Record the actual time, evidence, one next action and any limitation; there is no automatic readiness or completion decision.

## AI work (90 minutes)

### U022: boundary review (60 minutes total)

**Primary (30 min):** Extend and run the U022 path selector against malformed, oversized and symlink cases. Write the short threat-boundary note from the [U022 contract](../units/u022.md), including the fact that this prototype is local/single-user and does not establish authentication, tenant isolation or public exposure.

**Support (30 min):** Read `frontend/src/fde-v2/AskRagPanel.tsx` and verify it renders generated text as text rather than executing model-produced HTML. Record the source file/line and one limitation; do not modify general React/UI plumbing. Run the focused test `cd frontend && npx vitest run src/fde-v2/AskRagPanel.test.tsx -t 'renders model text as text'`. Review tracked additions for accidental credential/config inclusion. No automated secret scanner is configured in this checkout: record that limitation, and leave the secret-scan acceptance gate unmet rather than labeling a manual review as a scanner pass.

### U023: fixture and expected-boundary notes only (30 minutes)

Read OWASP [LLM01:2025 Prompt Injection](https://genai.owasp.org/llmrisk/llm01-prompt-injection/). Author three small synthetic document fixtures: an indirect instruction, misleading evidence, and a request beyond the local fixture boundary. Alongside each, state the expected **deterministic** boundary: the current app has no tool permission, filesystem permission or external-action permission. Record `not_run_day30_scope` for model observations. Use the [U023 Day 30 notes template](../templates/u023-day30-boundary-notes.md); do not test or claim model resistance today.

**Fixture structure check:** after creating `artifacts/u023-day30/adversarial-dev.jsonl`, run:

```sh
python3 scripts/fde_v2_check_adversarial_fixtures.py artifacts/u023-day30/adversarial-dev.jsonl
```

The checker validates three unique bounded synthetic fixture records and the declared permission limits. It explicitly reports `model_behavior=not_run_day30_scope` and `u023_complete=false`. A passing structure check does not test prompt-injection behavior, complete U023 or pass P1. Remaining execution/regression evidence is scheduled on Day31.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Input, data and secret boundaries | 30 | [U022](../units/u022.md) |
| support | Input, data and secret boundaries | 30 | [U022](../units/u022.md) |
| primary | Adversarial documents and regression tests | 30, authoring scope only | [U023](../units/u023.md) |

## Evidence and limits

Keep the note, fixtures and test outputs synthetic. Do not mark U022 complete unless all scheduled acceptance evidence exists. Do not mark U023 complete on Day30: execution and regression evidence continue on Day31. Real generated-answer behavior remains unverified while model access is blocked; deterministic tests and fixture authoring cannot satisfy that gate.
