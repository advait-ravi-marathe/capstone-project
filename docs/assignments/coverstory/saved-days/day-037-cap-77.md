# Day 037 · 19 Nov 2026 — Check extractive payment-help consistency (150m)

**Date:** 2026-11-19 · **Phase:** 1 · **Primary ID:** `CAP-77` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `27bbcf178908633a318e45700c63ac95a075e3745416a674e96d5eaa74674dc1`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Reject unsupported claims using exact source slices; do not equate extractive agreement with valid advice.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_77.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_77.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-77/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-77.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-77/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-77/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-77` · primary · Day 037 · 19 Nov 2026 — Check extractive payment-help consistency (150m)

Description SHA-256: `27bbcf178908633a318e45700c63ac95a075e3745416a674e96d5eaa74674dc1`

**Practical task:** Reject unsupported claims using exact source slices; do not equate extractive agreement with valid advice.

Exact source description: [saved curriculum record `CAP-77`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_77.py
- ai/coverstory/tests/test_cap_77.py
- fixtures/coverstory/CAP-77/synthetic-v1.json
- docs/assignments/coverstory/CAP-77.md
- docs/evidence/coverstory/CAP-77/harness-self-check.json
- docs/evidence/coverstory/CAP-77/result.json

check_answer(StructuredAnswer, authorizedSources)->ConsistencyReport Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Every claim.text exactly equals cited immutable source slice and passes CAP-59. Any failure makes UNSUPPORTED; empty claims require ABSTAIN_REQUIRED. Source applicable and authorized before checking. AI is read-only and cannot mark payment paid, accept terms, change quotes or submit a proposal.

#### Acceptance cases · expected, NOT_RUN

- CAP-77-CS-01: Input: source='Payment is pending.'; exact claim/exact citation. Expected: SUPPORTED valid 1, still not authoritative state.
- CAP-77-CS-02: Input: claim='Payment is confirmed.' same citation. Expected: CLAIM_TEXT_MISMATCH, UNSUPPORTED.
- CAP-77-CS-03: Input: exact text from B-only document. Expected: UNKNOWN_SOURCE, UNSUPPORTED.
- CAP-77-CS-04: Input: claims [] or one valid + one invalid. Expected: ABSTAIN_REQUIRED or UNSUPPORTED with valid 1.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Claim/source provenance graph template and today’s existing test/receipt in docs/evidence/coverstory/CAP-77/result.json. Annotate the supplied claim → citation → exact authorized source slice → per-claim check → overall answer status. Record only these changed facts: Every claim must match its cited extract exactly.; One unsupported claim makes the complete answer unsupported.; Empty claims require abstention rather than a vacuous supported answer..

Alternative: Exact extractive consistency versus semantic entailment assessment for paraphrases. Interview question: What does a passing string/slice check prove, what does it not prove, and why can it never update payment or proposal state? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
