# Actual learning Day 23: React performance

**Status: Not started.** Planned date: **2026-11-18** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **React performance**.

Explain measurement, profiling and one justified optimization; avoid premature memoization.

Fresh prompt: A result list renders 500 passages after each keystroke. Identify what you would measure first and one change you would verify with before/after evidence.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U017. Compare keyword and vector results on identical development cases and k, using the documented answerable-case denominator; with fixed vectors, report only path correctness. Inspect a retrieval miss and write the next experiment. Do not compare raw BM25 and cosine values as calibrated scores or use held-out cases.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Retrieval comparison and inspection | 60 | [U017 contract](../units/u017.md) |
| support | Retrieval comparison and inspection | 30 | [U017 contract](../units/u017.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
