# Day 009 · 22 Oct 2026 — Load synthetic product wording safely (150m)

**Date:** 2026-10-22 · **Phase:** 1 · **Primary ID:** `CAP-53` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `c364e713a798cc6c48d7bde614644bbc04794ab0afb21a4eb59aff0a81c92657`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Load local public source fixtures deterministically, preserving provenance and all-or-nothing failure.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_53.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_53.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-53/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-53.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-53/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-53/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-53` · primary · Day 009 · 22 Oct 2026 — Load synthetic product wording safely (150m)

Description SHA-256: `c364e713a798cc6c48d7bde614644bbc04794ab0afb21a4eb59aff0a81c92657`

**Practical task:** Load local public source fixtures deterministically, preserving provenance and all-or-nothing failure.

Exact source description: [saved curriculum record `CAP-53`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_53.py
- ai/coverstory/tests/test_cap_53.py
- fixtures/coverstory/CAP-53/synthetic-v1.json
- docs/assignments/coverstory/CAP-53.md
- docs/evidence/coverstory/CAP-53/harness-self-check.json
- docs/evidence/coverstory/CAP-53/result.json

load_wording(root:Path)->list[PublicWording] Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Only instructor-listed direct-child UTF-8 JSON files; nonblank source_id, version, text, evidenceStatus=SYNTHETIC and public classification. Filename sort; duplicates rejected. No network/absolute/path-traversal reads. Recovered insurer evidence is not silently copied into this fixture corpus.

#### Acceptance cases · expected, NOT_RUN

- CAP-53-CS-01: Input: b.json source-B/v1; a.json source-A/v2. Expected: A then B, exact text and labels.
- CAP-53-CS-02: Input: empty directory. Expected: [].
- CAP-53-CS-03: Input: malformed JSON or missing version. Expected: LoadError filename/code; no partial records.
- CAP-53-CS-04: Input: ../outside.json or duplicate source/version. Expected: INVALID_PATH with outside-read spy 0, or DUPLICATE_SOURCE_VERSION.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied File ingestion data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-53/result.json. Annotate the supplied listed path → safe open → decode → provenance validation → complete registry flow. Record only these changed facts: Only allowed direct-child paths are read.; Duplicate source/version prevents publication of a partial registry.; Exact text and deterministic filename order are retained..

Alternative: Best-effort partial loading versus all-or-nothing validation of a versioned corpus. Interview question: Why is a half-loaded evidence registry dangerous, and how does the outside-read spy prove more than merely checking an error string? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
