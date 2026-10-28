# Actual learning Day 31: Adversarial documents and regression tests

**Status: Not started.** Planned date: **2026-11-26** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **Continue target-role rotation**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use actual interview/recruiter feedback and prior weak dimensions; no extra time.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Run the Day30 fixture structure check against the artifact actually authored on Day30. Then run deterministic regression selectors for untrusted retrieved text and access boundaries. If a Day30 artifact is absent, create it only from the three assigned synthetic cases; do not invent model observations. Attempt the real-model observation only if pinned artifacts are actually available. Otherwise record a blocked result with the exact missing runtime. Synthetic tests prove only deterministic app boundaries; they do not show what the model would say.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | AIFDE-V2-U023 | 60 min | [AIFDE-V2-U023](../units/u023.md) |
| support | AIFDE-V2-U023 | 30 min | [AIFDE-V2-U023](../units/u023.md) |

### Learn from exact sources

OWASP [LLM01:2025 Prompt Injection](https://genai.owasp.org/llmrisk/llm01-prompt-injection/) and the existing [U023 contract](../units/u023.md).

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
