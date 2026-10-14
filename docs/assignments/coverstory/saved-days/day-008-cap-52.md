# Day 008 · 21 Oct 2026 — Expose tested catalog import API (150m)

**Date:** 2026-10-21 · **Phase:** 1 · **Primary ID:** `CAP-52` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `2c6c4d0edf4004448830f24bb91e601c0eeeb4eec0640bfaea2b0ae00a9d4059`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Map domain decisions into a real Spring request-binding seam while the instructor supplies the thin public catalog reader.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/catalog/api/CAP52Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP52Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-52/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-52.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-52/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-52/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-52` · primary · Day 008 · 21 Oct 2026 — Expose tested catalog import API (150m)

Description SHA-256: `2c6c4d0edf4004448830f24bb91e601c0eeeb4eec0640bfaea2b0ae00a9d4059`

**Practical task:** Map domain decisions into a real Spring request-binding seam while the instructor supplies the thin public catalog reader.

Exact source description: [saved curriculum record `CAP-52`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/catalog/api/CAP52Seam.java
- src/test/java/capstone/coverstory/catalog/CAP52Test.java
- fixtures/coverstory/CAP-52/synthetic-v1.json
- docs/assignments/coverstory/CAP-52.md
- docs/evidence/coverstory/CAP-52/harness-self-check.json
- docs/evidence/coverstory/CAP-52/result.json

POST /api/v1/catalog/imports ImportRequest{sourceId, sourceVersion, canonicalHash}; CatalogImportService.accept(trustedEditor, request). Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Fake repository first: ACCEPT → 201, DUPLICATE → 200, CONFLICT → 409, INVALID → 400; missing principal 401, wrong role 403. Instructor supplies GET /api/v1/catalog?category=health and four-category React shell based on recovered designs, with catalog-source/status labels. Learner only completes/import adapter tests. No database durability claim before CAP-57.

#### Acceptance cases · expected, NOT_RUN

- CAP-52-CS-01: Input: editor-E submits new valid source version/hash. Expected: 201 and one service call.
- CAP-52-CS-02: Input: same request then same key changed hash. Expected: 200 DUPLICATE then 409 CONFLICT; original retained.
- CAP-52-CS-03: Input: anonymous or applicant posts import. Expected: 401/403 respectively; zero writes.
- CAP-52-CS-04: Input: body ownerId/editorId override or blank hash. Expected: 400 before service call.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied HTTP sequence template and today’s existing test/receipt in docs/evidence/coverstory/CAP-52/result.json. Annotate the supplied trusted editor → request binder → import service → fake repository → response sequence. Record only these changed facts: Actor comes from the trusted principal, not body fields.; The adapter maps ACCEPT/DUPLICATE/CONFLICT/INVALID to explicit statuses.; The fake-repository receipt proves no SQL durability..

Alternative: Controller-local duplicate logic versus a thin adapter delegating to the shared policy. Interview question: What would drift if the controller and core classified conflicts separately, and where must an owner/editor override be rejected? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
