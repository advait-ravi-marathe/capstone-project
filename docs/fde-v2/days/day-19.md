# Actual learning Day 19: AWS certification: compute/reliability diagnostic

**Status: Not started.** Planned date: **2026-11-14** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **AWS certification: compute/reliability diagnostic**.

Compare relevant compute/deployment options and recovery choices; no deployment or spend implied.

Fresh prompt: For a small API with bursty reads and a single writer, compare one managed compute option with a container option; identify availability and failure tradeoffs.

Sources: [Role interview bank and scoring rubric](../role-bank.md), [AWS syllabus bank and official SAA-C03 domains](../aws-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Finish U014 case review and verify all 30 cases against the fictional source files. Keep labels independent of any retriever result. Start U015 by reading SQLite FTS5 BM25 semantics and running one query against the supplied chunk fixture. Record that FTS5 rank values sort lower-first and are not confidence probabilities.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Evaluation cases before retrieval tuning | 60 | [U014 contract](../units/u014.md) |
| primary | Non-neural retrieval baseline | 30 | [U015 contract](../units/u015.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
