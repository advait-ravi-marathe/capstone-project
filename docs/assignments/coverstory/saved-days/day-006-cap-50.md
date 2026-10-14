# Day 006 · 19 Oct 2026 — Count import decisions + test Spring seam (150m)

**Date:** 2026-10-19 · **Phase:** 1 · **Primary ID:** `CAP-50` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `e15faf03514d2edc001644c1a43abcd5d05849256e9763f97f54c53088e008f9`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain Java per-actor map aggregation and a fake-repository service test while CAP-51 guards SQL fanout.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/catalog/api/CAP50Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP50Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-50/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-50.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-50/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-50/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-50` · primary · Day 006 · 19 Oct 2026 — Count import decisions + test Spring seam (150m)

Description SHA-256: `e15faf03514d2edc001644c1a43abcd5d05849256e9763f97f54c53088e008f9`

**Practical task:** Retain Java per-actor map aggregation and a fake-repository service test while CAP-51 guards SQL fanout.

Exact source description: [saved curriculum record `CAP-50`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

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
- src/main/java/capstone/coverstory/catalog/api/CAP50Seam.java
- src/test/java/capstone/coverstory/catalog/CAP50Test.java
- fixtures/coverstory/CAP-50/synthetic-v1.json
- docs/assignments/coverstory/CAP-50.md
- docs/evidence/coverstory/CAP-50/harness-self-check.json
- docs/evidence/coverstory/CAP-50/result.json

Map<String, Long> acceptedCounts(List<ImportDecision> records); fake CatalogImportService calls classify/accept. Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Count ACCEPT processing records by exact editor fixture, not duplicate logical-event counting. Immutable output; reject null/blank editor/decision. Supplied Spring/fake seam checks same import twice then changed hash yields one insert. Java/Top150 outcome remains in practical work; no extra optional Ktor task.

#### Acceptance cases · expected, NOT_RUN

- CAP-50-CS-01: Input: E/s1 ACCEPT, E/s1 DUPLICATE, F/s1 ACCEPT, E/s2 ACCEPT. Expected: immutable {E:2, F:1}.
- CAP-50-CS-02: Input: only CONFLICT. Expected: {}.
- CAP-50-CS-03: Input: blank editor. Expected: IllegalArgumentException; no partial map.
- CAP-50-CS-04: Input: same import hash-p twice then hash-q. Expected: ACCEPT, DUPLICATE, CONFLICT; repository insert exactly 1.

#### Architecture/interview evidence delta

At most 3 minutes inside the existing docs/test-evidence allocation; at most 2 changed facts and either 4 nodes or 7 sequence messages. Do not draw a new full system or add implementation work. Primary and linked component share one evidence sheet: combined maximum 5 minutes, 3 changed facts, 6 nodes or 10 messages for the whole day. Use the instructor-supplied Data-flow/service sequence template and today’s existing test/receipt in docs/evidence/coverstory/CAP-50/result.json. Annotate the supplied decision records → ACCEPT filter → editor counter → immutable result flow, using the existing one-insert service receipt. Record only these changed facts: Only ACCEPT processing records contribute to a counter.; Editor identity partitions counts..

Alternative: A mutable grouped map versus an immutable result with separate logical-event deduplication. Interview question: Why does this counter count processing decisions rather than deduplicate event history, and what does the fake repository’s one-insert receipt add? Use at most 2 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.


### `CAP-51` · linked_component · [Component of Day 006 / CAP-50] 19 Oct 2026 — Avoid catalog reconciliation fanout (included in shared 150m day; no extra time)

Description SHA-256: `2538b362ab8d752a10b5a064afcb49087138c356a8a7e60f49f63e1831cbae3e`

**Practical task:** Preserve SQL per-logical-record counts, nulls and successful-attempt existence without changing source prices.

Exact source description: [saved curriculum record `CAP-51`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/resources/coverstory/catalog/cap_51.sql
- src/test/java/capstone/coverstory/catalog/CAP51Test.java
- fixtures/coverstory/CAP-51/synthetic-v1.json
- docs/assignments/coverstory/CAP-51.md
- docs/evidence/coverstory/CAP-51/harness-self-check.json
- docs/evidence/coverstory/CAP-51/result.json

SELECT published_count, known_base_total_minor, confirmed_import_count for editor_fixture and [from, to). Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

An import has confirmation when at least one successful import-attempt receipt exists; this does not verify insurer evidence. Sum known amount once per source record even with many successful attempts. Null amount counts row, contributes no amount; unknown remains Unknown in UI.

#### Acceptance cases · expected, NOT_RUN

- CAP-51-CS-01: Input: E/source-A amount 10000 with 2 success attempts; E/source-B amount 2000 with no attempts. Expected: published_count 2, total 12000, confirmed_import_count 1.
- CAP-51-CS-02: Input: E/source-C NULL only. Expected: count 1, total 0, confirmed 0; raw amount stays NULL.
- CAP-51-CS-03: Input: F/source-A success only. Expected: E confirmed 0.
- CAP-51-CS-04: Input: add third success attempt for E/source-A. Expected: all aggregates unchanged.

#### Architecture/interview evidence delta

At most 2 minutes inside the existing docs/test-evidence allocation. On the primary’s existing evidence sheet, annotate the attempt-fanout edge to show that each evidence amount contributes once. Reuse the existing evidence/result nodes; do not add a second reconciliation diagram. Changed fact: Attempt fanout never multiplies the evidence amount. The linked component contributes at most 1 changed fact and 2 nodes or 3 sequence messages. Primary and component together remain at most 5 minutes, 3 changed facts, 6 nodes or 10 messages; no new diagram or implementation task.

Alternative: EXISTS or pre-aggregated attempts versus SUM(DISTINCT amount) over a raw join. Interview question: Why would SUM(DISTINCT amount) incorrectly merge two legitimate records with the same amount, and why is import confirmation not insurer price verification? Use at most 1 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.
