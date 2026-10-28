# Actual learning Day 25: HLD communication

**Status: Not started.** Planned date: **2026-11-20** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **HLD communication**.

Give a five-minute design explanation and answer follow-up tradeoffs within the 30 min block.

Fresh prompt: Explain a two-minute system design for retrieval of versioned policies. State scale assumptions, stale-version behavior and one alternative; use the separate question-bank format.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Use 30 minutes to finish U018 citation/source boundary tests. Use 60 minutes to begin U019: connect the learner answer seam to the supplied local pipeline, verify JSON parsing and citation membership with an explicit test double. This deterministic contract check is not a real generative RAG pass; the actual model/artifact gate remains blocked.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| buffer | Grounding prompt and trustworthy source plumbing | 30 | [U018 contract](../units/u018.md) |
| primary | First genuine generative RAG path | 60 | [U019 contract](../units/u019.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
