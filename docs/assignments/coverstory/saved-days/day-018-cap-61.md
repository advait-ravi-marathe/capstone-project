# Day 018 · 31 Oct 2026 — Gate wording answers on explicit thresholds (150m)

**Date:** 2026-10-31 · **Phase:** 1 · **Primary ID:** `CAP-61` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `113a8c82f7ce903859f50e10123d855e0e1847f4c6afd64049be57be6f6ab534`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain inclusive threshold and invalid-number reasoning for a read-only synthetic wording answer.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_61.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_61.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-61/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-61.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-61/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-61/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-61` · primary · Day 018 · 31 Oct 2026 — Gate wording answers on explicit thresholds (150m)

Description SHA-256: `113a8c82f7ce903859f50e10123d855e0e1847f4c6afd64049be57be6f6ab534`

**Practical task:** Retain inclusive threshold and invalid-number reasoning for a read-only synthetic wording answer.

Exact source description: [saved curriculum record `CAP-61`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_61.py
- ai/coverstory/tests/test_cap_61.py
- fixtures/coverstory/CAP-61/synthetic-v1.json
- docs/assignments/coverstory/CAP-61.md
- docs/evidence/coverstory/CAP-61/harness-self-check.json
- docs/evidence/coverstory/CAP-61/result.json

decide_answer(authorized_hits, threshold)->ANSWER|ABSTAIN Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Scores are supplied evaluation scores [0, 1], not calibrated confidence. Top valid authorized applicable hit>=threshold permits extractive answer only; absent evidence ABSTAIN. Unknown insurance fields cannot be promoted to known from score. Tune development fixture only.

#### Acceptance cases · expected, NOT_RUN

- CAP-61-CS-01: Input: top score 0.8, threshold 0.7. Expected: ANSWER with exact citation.
- CAP-61-CS-02: Input: top score 0.7, then 0.699, with threshold 0.7. Expected: ANSWER then ABSTAIN.
- CAP-61-CS-03: Input: empty or denied-only hit with score 0.99. Expected: ABSTAIN.
- CAP-61-CS-04: Input: NaN/out-of-range threshold or score. Expected: ValueError; no answer.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Decision/state flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-61/result.json. Annotate the supplied authorized valid hit → threshold comparison → ANSWER or ABSTAIN decision. Record only these changed facts: The threshold boundary is inclusive.; NaN and out-of-range values are invalid.; A score is an evaluation value, not calibrated insurance confidence..

Alternative: Answer whenever retrieval is nonempty versus use an explicit tested abstention boundary. Interview question: What changes at 0.700 versus 0.699, and why must Unknown remain Unknown even when a permitted hit crosses the threshold? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
