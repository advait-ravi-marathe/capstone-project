# Actual learning Day 17: LLD boundaries

**Status: Not started.** Planned date: **2026-11-12** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **LLD boundaries**.

Model a small domain with clear invariants/interfaces; use patterns only where they solve a problem.

Fresh prompt: Review the Chunk and RetrievedChunk records. Name invariants and one interface boundary that makes the retriever testable; no essay.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Use the first 30 minutes to close U012 support: repair any source-offset boundary defect. Use the next 60 minutes for U013: review local embedding adapter and compute cosine by hand for two short vectors, then implement its adapter seam and run deterministic shape/finite/batch checks. No model weights exist, so do not claim that real embeddings ran; keep the real U013 gate blocked.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| support | Chunking with source provenance | 30 | [U012 contract](../units/u012.md) |
| primary | Real embeddings and vector intuition | 60 | [U013 contract](../units/u013.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
