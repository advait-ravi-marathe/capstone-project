# Actual learning Day 28: Short FE/full-stack mock segment

**Status: Not started.** Planned date: **2026-11-23** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **Short FE/full-stack mock segment**.

Time-box one original task and score it; 30 min is not a full employer loop.

Fresh prompt: Mock: diagnose why a UI shows citations from an earlier request after a new question. Explain request ordering, cancellation and one test; then score the attempt.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Start U021 in its scheduled 60-minute primary segment. Implement the failure-category mapping and begin redacted per-case records with trace ID, status, failure stage/category, timings and model/config metadata. Confirm question text, model output and retrieved text are absent. U021 has another 30-minute primary segment on Day 29; do not mark it complete on Day 28. The final 30 minutes are the scheduled visibility/evidence note; do not double-count it as an additional AI task.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Failure taxonomy and traces | 60 | [U021 contract](../units/u021.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
