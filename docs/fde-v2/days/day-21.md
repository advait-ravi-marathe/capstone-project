# Actual learning Day 21: Short role-specific rehearsal

**Status: Not started.** Planned date: **2026-11-16** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **Short role-specific rehearsal**.

Use 20 min unseen target-role prompt + 10 min scoring; repair the weakest observed dimension.

Fresh prompt: Unseen role-play: an operations team reports unsupported assistant answers. Clarify impact, propose one measured first step, and state uncertainty.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U016 mechanics with fixed instructor vectors: install the pinned local Qdrant client, create a temporary persistent collection, insert 2–3 chunks, close/reopen and query. Implement index_vectors and validate exact payload/dimension behavior. These synthetic vectors establish only Qdrant persistence mechanics, not real semantic retrieval. The final 30 minutes are the scheduled visibility/evidence note, not additional AI work.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | A genuine persisted vector database | 60 | [U016 contract](../units/u016.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
