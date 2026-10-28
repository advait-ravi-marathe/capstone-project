# Actual learning Day 15: HLD discovery and scale

**Status: Not started.** Planned date: **2026-11-10** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Do not copy its problems or record attempts here.
- Role interview/AWS bank: 30 minutes.
- AI implementation/evidence: 90 minutes total, including the listed segments and scheduled visibility note.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours or compress acceptance criteria.

## Role/AWS practice (30 minutes)

Topic: **HLD discovery and scale**.

Ask requirements first; make explicit traffic/data assumptions and choose a simple architecture.

Fresh prompt: A fictional policy assistant must answer 100 questions/minute with source links. Ask two clarifying questions, state assumptions, estimate a rough read/write split and identify one bottleneck.

Sources: [Role interview bank and scoring rubric](../role-bank.md). Record the prompt, actual time, a concise answer and one specific follow-up. A practice score is not a hiring prediction or certification readiness pass.

## AI work (90 minutes)

Complete U011 across all ten synthetic records. First inspect U011; preserve metadata contract and source license. Add a version-2 copy for one record and demonstrate exact repeat, valid next-version update, stale-version rejection, same-version changed-content rejection and skipped-version rejection. Execute the provided SQL aggregate query; do not start chunking until the records and version rules are clear.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Versioned small corpus and SQL metadata | 60 | [U011 contract](../units/u011.md) |
| buffer | Versioned small corpus and SQL metadata | 30 | [U011 contract](../units/u011.md) |

Run only the selector assigned to the current segment after implementing its seam. The default learner module intentionally raises `NotImplementedError`; an expected-red TODO is not a completed task. Current real embedding/generation gates require actual local model artifacts and remain blocked/unverified; no mock, fixed vector, extracted text or metadata check can satisfy them.

## Evidence

Save exact commands, actual output, source/config digest, observed limitations and one next action in the existing evidence folder. Mark no unit complete until learner-owned implementation passes independent acceptance; record real inference only when it actually ran.
