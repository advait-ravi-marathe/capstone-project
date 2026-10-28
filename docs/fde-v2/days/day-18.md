# Actual learning Day 18: Concurrency and reliability

**Status: Not started.** Planned date: **2026-11-13** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **Concurrency and reliability**.

Explain a race, retry or idempotency problem and a tested remedy.

Fresh prompt: Two ingestion workers submit the same document version concurrently. Describe the duplicate/update hazard and one transaction or idempotency remedy.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Spend 30 minutes finishing U013 deterministic validation and writing the exact missing-artifact limitation; no substitute vectors count as actual embeddings. Spend the remaining 30 minutes beginning U014: review case labels and the development/held-out separation. Do not tune retrieval or inspect held-out answers.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Real embeddings and vector intuition | 30 | [U013 contract](../units/u013.md) |
| support | Real embeddings and vector intuition | 30 | [U013 contract](../units/u013.md) |
| primary | Evaluation cases before retrieval tuning | 30 | [U014 contract](../units/u014.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
