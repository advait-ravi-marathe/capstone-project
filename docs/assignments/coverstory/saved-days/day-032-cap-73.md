# Day 032 · 14 Nov 2026 — Separate insurance wording evaluation sets (150m)

**Date:** 2026-11-14 · **Phase:** 1 · **Primary ID:** `CAP-73` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `760148ab9b1e84853b72e00a8c231cd542efd4e5928e3686c96639003817eada`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Machine-check development/held-out separation before model-assisted extraction or guidance is considered.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_73.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_73.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-73/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-73.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-73/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-73/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-73` · primary · Day 032 · 14 Nov 2026 — Separate insurance wording evaluation sets (150m)

Description SHA-256: `760148ab9b1e84853b72e00a8c231cd542efd4e5928e3686c96639003817eada`

**Practical task:** Machine-check development/held-out separation before model-assisted extraction or guidance is considered.

Exact source description: [saved curriculum record `CAP-73`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_73.py
- ai/coverstory/tests/test_cap_73.py
- fixtures/coverstory/CAP-73/synthetic-v1.json
- docs/assignments/coverstory/CAP-73.md
- docs/evidence/coverstory/CAP-73/harness-self-check.json
- docs/evidence/coverstory/CAP-73/result.json

validate_split(dev, golden)->SplitReport Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Instructor assigns SYNTHETIC health/payment-help cases; compare caseID, normalized question(lowercase+collapsed whitespace), source_family_id. Keep golden labels out of threshold tuning; counts/hashes and violations. Family means product/source lineage, so different versions may leak.

#### Acceptance cases · expected, NOT_RUN

- CAP-73-CS-01: Input: development case d1, family health-A; golden case g1, family health-B distinct questions. Expected: valid, development count 1, golden count 1.
- CAP-73-CS-02: Input: golden question differs only by case/whitespace. Expected: QUESTION_OVERLAP.
- CAP-73-CS-03: Input: different question same family. Expected: SOURCE_FAMILY_OVERLAP.
- CAP-73-CS-04: Input: same caseID or empty golden. Expected: CASE_ID_OVERLAP or INVALID_SPLIT; no held-out claim.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Dataset lineage graph template and today’s existing test/receipt in docs/evidence/coverstory/CAP-73/result.json. Annotate the supplied development/golden case nodes and source-family relationships. Record only these changed facts: Case ID overlap is invalid.; Normalized question equality can reveal hidden duplication.; Different questions from one source family can still leak evaluation information..

Alternative: Split by question wording alone versus enforce source-family separation. Interview question: Why does tuning a threshold on the golden set invalidate the holdout claim even when all case IDs differ? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
