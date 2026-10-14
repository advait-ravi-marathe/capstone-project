# Day 030 · 12 Nov 2026 — Chunk payment-help wording with coordinates (150m)

**Date:** 2026-11-12 · **Phase:** 1 · **Primary ID:** `CAP-71` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `e8ad87a7f9c37e6ed8f0f521718953a5b2ce700e4975c8794c1d7a51ce9da48b`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Preserve deterministic codepoint chunking for public synthetic payment-uncertainty help text.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_71.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_71.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-71/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-71.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-71/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-71/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-71` · primary · Day 030 · 12 Nov 2026 — Chunk payment-help wording with coordinates (150m)

Description SHA-256: `e8ad87a7f9c37e6ed8f0f521718953a5b2ce700e4975c8794c1d7a51ce9da48b`

**Practical task:** Preserve deterministic codepoint chunking for public synthetic payment-uncertainty help text.

Exact source description: [saved curriculum record `CAP-71`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_71.py
- ai/coverstory/tests/test_cap_71.py
- fixtures/coverstory/CAP-71/synthetic-v1.json
- docs/assignments/coverstory/CAP-71.md
- docs/evidence/coverstory/CAP-71/harness-self-check.json
- docs/evidence/coverstory/CAP-71/result.json

chunk_text(source_id, version, text, max_chars, overlap)->list[Chunk] Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Fixed character-window baseline, not semantic chunking. max>0; 0<=overlap<max. Preserve source/version/text and code-point[start, end); no empty trailing chunk. Help content cannot tell buyer payment succeeded without authoritative state.

#### Acceptance cases · expected, NOT_RUN

- CAP-71-CS-01: Input: text abcdefghij, max 4, overlap 1. Expected: [0:4]abcd, [3:7]defg, [6:10]ghij.
- CAP-71-CS-02: Input: text abcd, max 4, overlap 1. Expected: one chunk[0:4].
- CAP-71-CS-03: Input: empty text valid metadata. Expected: [].
- CAP-71-CS-04: Input: overlap 4/max 4 or missing version. Expected: ValueError.
- CAP-71-CS-05: Input: all chunks. Expected: exact text equals source[start:end].

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Chunk coordinate data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-71/result.json. Annotate the supplied immutable source → fixed character windows → overlapping chunks → source-coordinate verification. Record only these changed facts: Coordinates use code points and a half-open interval.; Overlap must be smaller than maximum chunk length.; The final boundary emits no empty trailing chunk..

Alternative: Fixed windows versus semantic chunking; this day establishes only deterministic coordinates. Interview question: Why can overlapping text still refer to one immutable source, and how would byte-based slicing break a Unicode citation contract? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
