# Day 012 · 25 Oct 2026 — Measure public wording retrieval baseline (150m)

**Date:** 2026-10-25 · **Phase:** 1 · **Primary ID:** `CAP-56` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `883d57e135b399eefc277f62032b1a35919a4f0689c14d41aa0352611fc900f5`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Measure a reproducible source-wording baseline while preserving unanswerable and denied-source failures.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_56.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_56.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-56/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-56.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-56/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-56/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-56` · primary · Day 012 · 25 Oct 2026 — Measure public wording retrieval baseline (150m)

Description SHA-256: `883d57e135b399eefc277f62032b1a35919a4f0689c14d41aa0352611fc900f5`

**Practical task:** Measure a reproducible source-wording baseline while preserving unanswerable and denied-source failures.

Exact source description: [saved curriculum record `CAP-56`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_56.py
- ai/coverstory/tests/test_cap_56.py
- fixtures/coverstory/CAP-56/synthetic-v1.json
- docs/assignments/coverstory/CAP-56.md
- docs/evidence/coverstory/CAP-56/harness-self-check.json
- docs/evidence/coverstory/CAP-56/result.json

evaluate_retriever(cases, retriever, k)->BaselineReport Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Actual ranked source IDs from local synthetic corpus; hit@k per case; macro mean only over answerable cases; unanswerable cases counted separately, no answerable metric=null, count 0. Empty dataset invalid. Record corpus/config hashes; no semantic correctness or advice guarantee.

#### Acceptance cases · expected, NOT_RUN

- CAP-56-CS-01: Input: q1 relevant[A] returns[B, A]; q2 relevant[C] returns[D]; k2. Expected: hits 1 and 0; macro 0.5.
- CAP-56-CS-02: Input: only unanswerable q3 relevant[] returns []. Expected: metric null; answerable 0, unanswerable 1.
- CAP-56-CS-03: Input: foreign private synthetic document returned. Expected: authorization_violation 1; whole run fails regardless metric.
- CAP-56-CS-04: Input: same fixture/config twice. Expected: identical outputs and hashes apart from run timestamp.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Evaluation data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-56/result.json. Annotate the supplied labeled case → ranked IDs → per-case hit → answerable-denominator report. Record only these changed facts: Unanswerable cases are counted separately.; An empty answerable denominator yields null rather than zero.; An authorization violation fails the run regardless of hit rate..

Alternative: Include unanswerable cases as misses versus report a separate abstention population. Interview question: How would mixing the two populations distort the metric, and why does a reproducible hit rate still not prove semantic correctness? Sunday: no mandatory DSM or interview. This question is optional and may only reuse the existing individual-evidence allocation; it adds no minutes.
