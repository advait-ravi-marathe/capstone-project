# Actual learning Day 24: Backend/security integration

**Status: Not started.** Planned date: **2026-11-19** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **Backend/security integration**.

Review one auth/access/data boundary and an integration failure path.

Fresh prompt: Trace a POST /ask request through the supplied React/API/pipeline boundary. Identify where untrusted context enters and one observable failure status.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U018. Compose two separate messages, keeping trusted system guidance outside retrieved passages; cap evidence to four bounded chunks. Add adversarial input where a source says “ignore instructions” and prove that the untrusted boundary is explicit. Reject blank, duplicate, foreign and non-string citation IDs. This does not establish prompt-injection immunity.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Grounding prompt and trustworthy source plumbing | 90 | [U018 contract](../units/u018.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
