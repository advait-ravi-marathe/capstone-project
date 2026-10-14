# Day 003 · 16 Oct 2026 — Rank applicable wording + reconcile import attempts (150m)

**Date:** 2026-10-16 · **Phase:** 1 · **Primary ID:** `CAP-44` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `a46a8ac59c69edd39ca76a39fccaa2c15ac21bb1468dd657e808703afb46387d`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Rank permitted source-version wording deterministically; use CAP-45 to inspect import retries without duplicating catalog evidence.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_44.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_44.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-44/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-44.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-44/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-44/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-44` · primary · Day 003 · 16 Oct 2026 — Rank applicable wording + reconcile import attempts (150m)

Description SHA-256: `a46a8ac59c69edd39ca76a39fccaa2c15ac21bb1468dd657e808703afb46387d`

**Practical task:** Rank permitted source-version wording deterministically; use CAP-45 to inspect import retries without duplicating catalog evidence.

Exact source description: [saved curriculum record `CAP-44`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.
The two retained contracts below share docs10/build55/tests30=95m: primary docs5/build35/tests20 and component docs5/build20/tests10. Neither has separate scheduled minutes.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_44.py
- ai/coverstory/tests/test_cap_44.py
- fixtures/coverstory/CAP-44/synthetic-v1.json
- docs/assignments/coverstory/CAP-44.md
- docs/evidence/coverstory/CAP-44/harness-self-check.json
- docs/evidence/coverstory/CAP-44/result.json

rank_wording(query, fixture_principal, sources, k)->list[RankedHit] Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

SYNTHETIC SourceVersion contains visibility PUBLIC_PUBLISHED or RESTRICTED_EDITOR_DRAFT, allowed_fixture_principals for restricted drafts only, category, scenario, source_id, version and text. Any Visitor can retrieve applicable PUBLIC_PUBLISHED wording without login. Restricted editor drafts require a trusted authorized editor fixture before scoring and are never returned to a customer. Instructor supplies that scope adapter. Match the health scenario; tokenize using [a-z0-9]+; score distinct token overlap; sort by descending score, then source_id and version. Preserve the exact excerpt. Reject duplicate source/version and missing provenance.

#### Acceptance cases · expected, NOT_RUN

- CAP-44-CS-01: Input: Visitor query 'waiting period' over public health scenario sources w2/v1='waiting', w1/v1='waiting period', w3/v2='period'. Expected: w1 score 2, w2 score 1, w3 score 1; no authentication gate for public wording..
- CAP-44-CS-02: Input: Add a higher-scoring RESTRICTED_EDITOR_DRAFT allowed only to catalog-editor-E; run as Visitor or another editor.. Expected: The restricted draft is never scored or returned. Public results remain unchanged..
- CAP-44-CS-03: Input: Reverse the public source order, then include a motor-scenario source.. Expected: Ordering is unchanged; the mismatched scenario is excluded before scoring..
- CAP-44-CS-04: Input: Duplicate w1/v1 or remove its version.. Expected: ValueError; no partial results..

#### Architecture/interview evidence delta

At most 3 minutes inside the existing docs/test-evidence allocation; at most 2 changed facts and either 4 nodes or 7 sequence messages. Do not draw a new full system or add implementation work. Primary and linked component share one evidence sheet: combined maximum 5 minutes, 3 changed facts, 6 nodes or 10 messages for the whole day. Use the instructor-supplied Authorization data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-44/result.json. Annotate the supplied visibility/applicability gate → scorer → stable rank → source-version hit flow. Record only these changed facts: PUBLIC_PUBLISHED wording remains accessible to an anonymous Visitor.; Restricted editor drafts are filtered before scoring..

Alternative: Filter before scoring versus score globally and remove denied hits afterward. Interview question: How can post-filtering change rankings or leak information, and why must public wording not inherit customer-private access rules? Use at most 2 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.


### `CAP-45` · linked_component · [Component of Day 003 / CAP-44] 16 Oct 2026 — Join catalog imports without losing failures (included in shared 150m day; no extra time)

Description SHA-256: `5fb59481da8a18d01e521e26daae81c351124a0b6882dc074fde21b5650d9a31`

**Practical task:** Preserve left-join, composite-owner matching and one-to-many SQL reasoning on source-import attempts.

Exact source description: [saved curriculum record `CAP-45`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/resources/coverstory/catalog/cap_45.sql
- src/test/java/capstone/coverstory/catalog/CAP45Test.java
- fixtures/coverstory/CAP-45/synthetic-v1.json
- docs/assignments/coverstory/CAP-45.md
- docs/evidence/coverstory/CAP-45/harness-self-check.json
- docs/evidence/coverstory/CAP-45/result.json

SELECT source_id, base_amount_minor, attempt_count, last_attempt_at, has_attempt FROM catalog_import_evidence LEFT JOIN catalog_import_attempt. Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Join exact(editor_fixture, source_id, source_version), qualify PUBLISHED evidence by captured_at half-open UTC window; attempts may occur later. One row per evidence record; no attempt count 0, last NULL, false; nullable base amount remains NULL, never a free price.

#### Acceptance cases · expected, NOT_RUN

- CAP-45-CS-01: Input: E/source-A amount 10000 with two attempts; E/source-B amount 2000 no attempts. Expected: two rows; counts 2 and 0; summed known input control total 12000 once.
- CAP-45-CS-02: Input: only F/source-B has an attempt. Expected: E/source-B remains count 0; no cross-editor join.
- CAP-45-CS-03: Input: published row base_amount_minor=NULL. Expected: NULL preserved; attempt count remains factual; not payable.
- CAP-45-CS-04: Input: successful attempt after captured_at window. Expected: counted for qualifying evidence record.

#### Architecture/interview evidence delta

At most 2 minutes inside the existing docs/test-evidence allocation. On the primary’s existing evidence sheet, annotate the evidence-to-attempt join edge with its composite editor/source/version key. Reuse the supplied SQL cardinality sketch rather than draw another ER diagram. Changed fact: The join uses editor, source and source version together. The linked component contributes at most 1 changed fact and 2 nodes or 3 sequence messages. Primary and component together remain at most 5 minutes, 3 changed facts, 6 nodes or 10 messages; no new diagram or implementation task.

Alternative: LEFT JOIN with pre-aggregated attempts versus INNER JOIN or raw attempt fanout. Interview question: Where should the UTC evidence-window predicate live so that unmatched evidence survives and amount totals are not inflated? Use at most 1 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.
