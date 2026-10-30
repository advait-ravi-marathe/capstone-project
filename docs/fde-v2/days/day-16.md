# Actual learning Day 16: HLD failures and tradeoffs

**NOT READY under the beginner-first reset.** The first-15 keyword-search exercises do not teach or pass the historical CoverStory ingestion, chunking, embedding or retrieval prerequisites. Do not start this page as the next lesson until the separate prerequisites in the [handoff note](../amendments/day16-prerequisite-handoff.md) are taught and accepted. This page is retained as historical planned content; its presence is not readiness evidence.

**Status: Not started.** The previous calendar date is superseded by the beginner-first reset; no active date is assigned.

**Prerequisite:** U011 versioned ingestion must pass its learner acceptance check before you count this chunking task as complete. If U011 is unfinished, continue it first; the supplied chunker/reference output is only a worked example.

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **HLD failures and tradeoffs**.

Walk through cache/queue/storage/failure decisions for the same scoped system.

Fresh prompt: The supplied local service has stale corpus versions and a vector-store restart. Explain consistency choices, one failure mode and a bounded recovery plan.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U012. Chunk the versioned deductible document with the documented default limits; inspect three returned rows (or all if fewer), offsets and exact source substrings. Then test empty, short and multi-window strings. Explain the character-versus-token limitation and why overlap may duplicate evidence. No embeddings today.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Chunking with source provenance | 90 | [U012 contract](../units/u012.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
