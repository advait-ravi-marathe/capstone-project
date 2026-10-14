# Day 016 · 29 Oct 2026 — Verify exact wording citation provenance (150m)

**Date:** 2026-10-29 · **Phase:** 1 · **Primary ID:** `CAP-59` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `f88b3fcce0e24f2fa31e33225a82317bad3db7b5ca68b548d8c1c97c5f96edb9`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Verify immutable health wording source, version, Unicode offsets and content digest without choosing a newer version silently.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_59.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_59.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-59/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-59.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-59/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-59/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-59` · primary · Day 016 · 29 Oct 2026 — Verify exact wording citation provenance (150m)

Description SHA-256: `f88b3fcce0e24f2fa31e33225a82317bad3db7b5ca68b548d8c1c97c5f96edb9`

**Practical task:** Verify immutable health wording source, version, Unicode offsets and content digest without choosing a newer version silently.

Exact source description: [saved curriculum record `CAP-59`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_59.py
- ai/coverstory/tests/test_cap_59.py
- fixtures/coverstory/CAP-59/synthetic-v1.json
- docs/assignments/coverstory/CAP-59.md
- docs/evidence/coverstory/CAP-59/harness-self-check.json
- docs/evidence/coverstory/CAP-59/result.json

verify_citation(Citation, registry)->Verification Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Citation source_id, version, start, end, excerpt, content_sha256; Python codepoint [start, end). Registry exact(source, version); source must already be authorized/applicable. Hash proves integrity, not authority or payable quote validity.

#### Acceptance cases · expected, NOT_RUN

- CAP-59-CS-01: Input: source-A/v2='waiting period'; citation[0:7]='waiting' exact hash. Expected: VALID.
- CAP-59-CS-02: Input: start=end=0. Expected: EMPTY_EXCERPT.
- CAP-59-CS-03: Input: unknown source/version. Expected: UNKNOWN_SOURCE/UNKNOWN_VERSION; no latest fallback.
- CAP-59-CS-04: Input: excerpt differs or hash tampered. Expected: EXCERPT_MISMATCH/CONTENT_HASH_MISMATCH.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Citation provenance data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-59/result.json. Annotate the supplied citation → exact authorized source/version → code-point slice → digest check → verification flow. Record only these changed facts: Offsets are half-open code-point indices.; The requested immutable version never silently becomes latest.; Digest agreement proves integrity, not source authority..

Alternative: Fallback to the latest source version versus reject an unknown or stale citation binding. Interview question: When can byte offsets disagree with Python string indices, and why can a valid hash still accompany inapplicable insurance evidence? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
