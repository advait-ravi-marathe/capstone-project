# Actual learning Day 27: AWS certification: readiness gap map

**Status: Not started.** Planned date: **2026-11-22** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **AWS certification: readiness gap map**.

Map remaining SAA-C03 knowledge/practical gaps to planned AWS work; decide study topics, not an exam date.

Fresh prompt: Compare actual practice misses with official SAA-C03 domains and current project evidence; identify two learning gaps. No booking/date/pass claim.

Sources: [Role interview bank and scoring rubric](../role-bank.md), [AWS syllabus bank and official SAA-C03 domains](../aws-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U020 on development unanswerable cases and document false positives/false negatives; do not tune against held-out cases. Review whether retrieval misses or generation failures caused each observed result. Since real generation is blocked, record the gate unmet rather than claiming abstention quality from mocks.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Abstention and user-facing limits | 60 | [U020 contract](../units/u020.md) |
| support | Abstention and user-facing limits | 30 | [U020 contract](../units/u020.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
