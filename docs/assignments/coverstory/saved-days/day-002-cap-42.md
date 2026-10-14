# Day 002 · 15 Oct 2026 — Reconcile catalog evidence totals + Java identity (150m)

**Date:** 2026-10-15 · **Phase:** 1 · **Primary ID:** `CAP-42` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `ea55dfdc4410ff26c19e59b8a55d6f039534217f2dd2246bbe21cf0ae771e7cf`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Write an exact owner/status/time-window SQL aggregate over catalog-import evidence while preserving the Java sequential identity outcome in CAP-43.

## Explicit repository paths in the saved contract

- `src/main/resources/coverstory/catalog/cap_42.sql` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP42Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-42/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-42.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-42/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-42/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-42` · primary · Day 002 · 15 Oct 2026 — Reconcile catalog evidence totals + Java identity (150m)

Description SHA-256: `ea55dfdc4410ff26c19e59b8a55d6f039534217f2dd2246bbe21cf0ae771e7cf`

**Practical task:** Write an exact owner/status/time-window SQL aggregate over catalog-import evidence while preserving the Java sequential identity outcome in CAP-43.

Exact source description: [saved curriculum record `CAP-42`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

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
- src/main/resources/coverstory/catalog/cap_42.sql
- src/test/java/capstone/coverstory/catalog/CAP42Test.java
- fixtures/coverstory/CAP-42/synthetic-v1.json
- docs/assignments/coverstory/CAP-42.md
- docs/evidence/coverstory/CAP-42/harness-self-check.json
- docs/evidence/coverstory/CAP-42/result.json

SELECT published_count BIGINT, known_base_total_minor NUMERIC FROM catalog_import_evidence for (:editor_fixture, :from_utc, :to_utc). Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Training table: editor_fixture, source_id, publication_state, base_amount_minor bigint nullable, currency, captured_at timestamptz. Count PUBLISHED records in [from, to), restrict INR, sum known integer amounts exactly; empty output count 0, total 0. This is an import control total, never a customer premium or payable quote. Instructor supplies Java BigDecimal exact-value comparator and the preserved generic signed_total fixture only as historical numeric-regression evidence; no production premium conversion.

#### Acceptance cases · expected, NOT_RUN

- CAP-42-CS-01: Input: editor-E: PUBLISHED 10000 at lower boundary, PUBLISHED 2000 midday, DRAFT 99999 midday, PUBLISHED 5000 at upper boundary; editor-F 50000 midday. Expected: published_count 2, known_base_total_minor 12000; boundaries and other editor excluded.
- CAP-42-CS-02: Input: editor with no records. Expected: one row: count 0, total 0.
- CAP-42-CS-03: Input: two large bigint input amounts that overflow 32-bit sum. Expected: exact numeric sum; no double/float rounding.
- CAP-42-CS-04: Input: mixed currency row inside fixture. Expected: explicit fixture/schema rejection, not INR summation.

#### Architecture/interview evidence delta

At most 3 minutes inside the existing docs/test-evidence allocation; at most 2 changed facts and either 4 nodes or 7 sequence messages. Do not draw a new full system or add implementation work. Primary and linked component share one evidence sheet: combined maximum 5 minutes, 3 changed facts, 6 nodes or 10 messages for the whole day. Use the instructor-supplied ER/aggregate lineage template and today’s existing test/receipt in docs/evidence/coverstory/CAP-42/result.json. Annotate the supplied source → catalog_import_evidence → control-total view with an optional import-attempt relationship. Record only these changed facts: The qualifying source evidence record is the counting unit.; The time predicate is lower-inclusive and upper-exclusive UTC..

Alternative: Aggregate evidence before joining attempts versus summing a one-to-many joined rowset. Interview question: Why can an empty aggregate return zero while an unknown individual amount must not become a free premium, and why must Java compare exact numeric values rather than doubles? Use at most 2 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.


### `CAP-43` · linked_component · [Component of Day 002 / CAP-42] 15 Oct 2026 — Classify sequential catalog import identity (included in shared 150m day; no extra time)

Description SHA-256: `57771401bccebef7ac66c1945e26e5c863ff1f8fb3d9bb038a4adb65d4be5455`

**Practical task:** Preserve Java Map classify-versus-accept, exact string equality and mutation rules for one synthetic editor import command.

Exact source description: [saved curriculum record `CAP-43`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/catalog/api/CAP43Seam.java
- src/test/java/capstone/coverstory/catalog/CAP43Test.java
- fixtures/coverstory/CAP-43/synthetic-v1.json
- docs/assignments/coverstory/CAP-43.md
- docs/evidence/coverstory/CAP-43/harness-self-check.json
- docs/evidence/coverstory/CAP-43/result.json

Decision classify(Map<ImportKey, String> accepted, ImportCommand command); Decision accept(...); ImportKey(editorFixture, sourceId, sourceVersion). Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Command payload is an opaque canonicalHash string supplied by instructor. Null/blank fields INVALID; exact strings, no trimming. classify is pure; accept inserts only ACCEPT. DUPLICATE=identical key/hash; CONFLICT=existing key/different hash. Not authentication, concurrent safety, publishing approval or insurer acceptance.

#### Acceptance cases · expected, NOT_RUN

- CAP-43-CS-01: Input: empty map; editor-E/source-health-A/v1/hash-p. Expected: classify ACCEPT no mutation; accept inserts one entry.
- CAP-43-CS-02: Input: same key/hash-p repeated. Expected: DUPLICATE, unchanged.
- CAP-43-CS-03: Input: same key/hash-q. Expected: CONFLICT; stored hash-p retained.
- CAP-43-CS-04: Input: editor-F same source/version. Expected: independent ACCEPT; not an authorization claim.
- CAP-43-CS-05: Input: null/blank field or spaced hash ' hash-p '. Expected: INVALID for null/blank; spaced nonblank hash conflicts by exact equality.

#### Architecture/interview evidence delta

At most 2 minutes inside the existing docs/test-evidence allocation. On the primary’s existing evidence sheet, annotate the policy/result edge to show that classify is read-only and accept changes the map only on ACCEPT. Keep the other decision states as supplied labels, not new nodes. Changed fact: classify reads without mutation. The linked component contributes at most 1 changed fact and 2 nodes or 3 sequence messages. Primary and component together remain at most 5 minutes, 3 changed facts, 6 nodes or 10 messages; no new diagram or implementation task.

Alternative: Blind Map.put replacement versus a separate classify/accept policy with explicit duplicate/conflict outcomes. Interview question: Which observable rule would blind replacement violate, and why does this sequential map still prove neither authentication nor concurrent safety? Use at most 1 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.
