# Actual learning Day 26: AWS certification: fresh practice baseline

**Status: Not started.** Planned date: **2026-11-21** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **AWS certification: fresh practice baseline**.

Use official current sample questions or authorized assessment; record domain errors. A short sample is not full readiness.

Fresh prompt: Use a short official SAA-C03 sample or course practice set; record the exact source, items attempted and domain-level misses. Do not infer readiness from a short sample.

Sources: [Role interview bank and scoring rubric](../role-bank.md), [AWS syllabus bank and official SAA-C03 domains](../aws-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Use 30 minutes on U019 contract tests and actual local artifact preflight; no model download or provider call. Then start U020: implement an explicit score-based abstention heuristic and test empty retrieval, below threshold, exact boundary and invalid scores. UI explanation is supplied. Keep the threshold labeled as a heuristic.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | First genuine generative RAG path | 30 | [U019 contract](../units/u019.md) |
| buffer | First genuine generative RAG path | 30 | [U019 contract](../units/u019.md) |
| primary | Abstention and user-facing limits | 30 | [U020 contract](../units/u020.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
