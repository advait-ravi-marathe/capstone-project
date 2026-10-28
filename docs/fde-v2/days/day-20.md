# Actual learning Day 20: AWS certification: operations/cost diagnostic

**Status: Not started.** Planned date: **2026-11-15** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **AWS certification: operations/cost diagnostic**.

Explain logs, metrics, secrets, quotas, billing alerts and teardown; budget alerts are not a hard cap.

Fresh prompt: Name operational signals and two cost controls for a synthetic AWS deployment; state how you would verify teardown and what an AWS Budget alert cannot guarantee.

Sources: [Role interview bank and scoring rubric](../role-bank.md), [AWS syllabus bank and official SAA-C03 domains](../aws-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U015. Implement the BM25 lexical seam, run every development question, retain misses and calculate Recall@3/MRR over the 12 answerable development cases only. Inspect one miss and add a bounded negative query. Keep the held-out JSONL out of any tuning command.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Non-neural retrieval baseline | 60 | [U015 contract](../units/u015.md) |
| buffer | Non-neural retrieval baseline | 30 | [U015 contract](../units/u015.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
