# Day 025 · 7 Nov 2026 — Compute reproducible wording retrieval metrics (150m)

**Date:** 2026-11-07 · **Phase:** 1 · **Primary ID:** `CAP-67` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `d0a029cc0920175f504d47440213e9dde2ab044eef1b7bdf8e78f3505f574059`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain recall, reciprocal rank and denominator integrity for synthetic product-wording evaluation.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_67.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_67.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-67/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-67.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-67/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-67/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-67` · primary · Day 025 · 7 Nov 2026 — Compute reproducible wording retrieval metrics (150m)

Description SHA-256: `d0a029cc0920175f504d47440213e9dde2ab044eef1b7bdf8e78f3505f574059`

**Practical task:** Retain recall, reciprocal rank and denominator integrity for synthetic product-wording evaluation.

Exact source description: [saved curriculum record `CAP-67`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_67.py
- ai/coverstory/tests/test_cap_67.py
- fixtures/coverstory/CAP-67/synthetic-v1.json
- docs/assignments/coverstory/CAP-67.md
- docs/evidence/coverstory/CAP-67/harness-self-check.json
- docs/evidence/coverstory/CAP-67/result.json

metrics_at_k(ranked_ids, relevant_ids, k); aggregate(case_metrics) Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Answerable cases only: distinct relevant retrieved / total relevant; RR = 1 / first relevant rank within k. Duplicate ranked IDs invalid; unanswerable separately reported; empty answerable mean null. Results neither insurance recommendation quality nor source authority.

#### Acceptance cases · expected, NOT_RUN

- CAP-67-CS-01: Input: ranked[C, A, B], relevant{A, B}, k2. Expected: recall 0.5, RR 0.5.
- CAP-67-CS-02: Input: ranked[], relevant{A}, k3. Expected: recall 0, RR 0.
- CAP-67-CS-03: Input: relevant{}. Expected: not applicable; excluded denominator.
- CAP-67-CS-04: Input: ranked[A, A]. Expected: ValueError.
- CAP-67-CS-05: Input: two answerable recalls 1 and 0, and RR values 1 and 0.5. Expected: mean recall 0.5, mean RR 0.75, count 2.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Metric computation data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-67/result.json. Annotate the supplied unique ranking → top-k slice → relevant set → recall/RR → aggregate report. Record only these changed facts: Duplicate ranked IDs are invalid.; Recall counts distinct relevant items while RR uses the first relevant rank.; Unanswerable cases do not enter these means..

Alternative: Report only a mean score versus retain per-case rank and denominator evidence. Interview question: How can recall 0.5 and RR 0.5 describe different properties, and why should an empty answerable population be null rather than a claimed zero-quality result? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
