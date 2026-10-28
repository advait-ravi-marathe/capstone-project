# Actual learning Day 22: JS/TS implementation interview practice

**Status: Not started.** Planned date: **2026-11-17** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **JS/TS implementation interview practice**.

Make a small requirements-driven component/data transformation without a supplied solution.

Fresh prompt: Given a list of retrieval hits, describe/write a tiny TypeScript transformation that preserves the top-k order while removing duplicate chunk IDs. No UI project.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Finish U016 by checking repeated upsert, wrong dimensions and document deletion. Start U017 by implementing the learner query-vector plus actual-store search seam. Use only fixed vectors for deterministic mechanics and preserve the real-embedding gate as blocked. Do not report retrieval improvement without real embeddings.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | A genuine persisted vector database | 30 | [U016 contract](../units/u016.md) |
| buffer | A genuine persisted vector database | 30 | [U016 contract](../units/u016.md) |
| primary | Retrieval comparison and inspection | 30 | [U017 contract](../units/u017.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
