# Day 005 · 18 Oct 2026 — Bucket source evidence by UTC day (150m)

**Date:** 2026-10-18 · **Phase:** 1 · **Primary ID:** `CAP-48` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `b71cdea616df39607e31723c35ea2121ca17310924315613d170c65b25453fb1`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Compute an exact UTC date-spine catalog evidence report including empty days; use CAP-49 for reproducible source-failure categories.

## Explicit repository paths in the saved contract

- `src/main/resources/coverstory/catalog/cap_48.sql` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP48Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-48/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-48.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-48/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-48/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-48` · primary · Day 005 · 18 Oct 2026 — Bucket source evidence by UTC day (150m)

Description SHA-256: `b71cdea616df39607e31723c35ea2121ca17310924315613d170c65b25453fb1`

**Practical task:** Compute an exact UTC date-spine catalog evidence report including empty days; use CAP-49 for reproducible source-failure categories.

Exact source description: [saved curriculum record `CAP-48`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

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
- src/main/resources/coverstory/catalog/cap_48.sql
- src/test/java/capstone/coverstory/catalog/CAP48Test.java
- fixtures/coverstory/CAP-48/synthetic-v1.json
- docs/assignments/coverstory/CAP-48.md
- docs/evidence/coverstory/CAP-48/harness-self-check.json
- docs/evidence/coverstory/CAP-48/result.json

SELECT day_utc, published_count, known_base_total_minor for editor_fixture and date_spine. Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

SYNTHETIC evidence amounts are integer INR minor units, not payable totals. Count PUBLISHED records, including nullable base amounts; exclude NULL from sum, all-NULL/empty output total 0 only. UTC conversion explicit regardless of session timezone.

#### Acceptance cases · expected, NOT_RUN

- CAP-48-CS-01: Input: E PUBLISHED amount 1000 at 2026-10-13T23:30:00Z and amount 500 at 2026-10-14T00:15:00Z; date spine 13 and 14 October 2026. Expected: 13 October: count 1, total 1000; 14 October: count 1, total 500.
- CAP-48-CS-02: Input: date spine 15 October 2026 with no data. Expected: count 0, total 0.
- CAP-48-CS-03: Input: same rows under Asia/Kolkata SQL session timezone. Expected: unchanged UTC bucket dates.
- CAP-48-CS-04: Input: nullable amount and F-owned amount 90000 row. Expected: E nullable row counted but not priced; F excluded.

#### Architecture/interview evidence delta

At most 3 minutes inside the existing docs/test-evidence allocation; at most 2 changed facts and either 4 nodes or 7 sequence messages. Do not draw a new full system or add implementation work. Primary and linked component share one evidence sheet: combined maximum 5 minutes, 3 changed facts, 6 nodes or 10 messages for the whole day. Use the instructor-supplied ER/time data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-48/result.json. Annotate the supplied date spine LEFT JOIN UTC-bucketed evidence view. Record only these changed facts: The date spine preserves zero-activity days.; UTC bucketing is independent of the SQL session timezone..

Alternative: Group only observed timestamps versus join an explicit date spine after UTC conversion. Interview question: Why can a session-local date cast move an observation to another day, and why must an empty day differ from a missing report row? Sunday: no mandatory DSM or interview. This question is optional and may only reuse the existing individual-evidence allocation; it adds no minutes.


### `CAP-49` · linked_component · [Component of Day 005 / CAP-48] 18 Oct 2026 — Classify health evidence failures (included in shared 150m day; no extra time)

Description SHA-256: `1bcdfed38221eeaa71eaa4924bd38391f7d2fc82bbcdb853c7805daa8ce620df`

**Practical task:** Distinguish missing, wrong-source and wrong-version evidence instead of inflating answer success.

Exact source description: [saved curriculum record `CAP-49`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_49.py
- ai/coverstory/tests/test_cap_49.py
- fixtures/coverstory/CAP-49/synthetic-v1.json
- docs/assignments/coverstory/CAP-49.md
- docs/evidence/coverstory/CAP-49/harness-self-check.json
- docs/evidence/coverstory/CAP-49/result.json

classify_failure(case, answer)->PASS|FALSE_ABSTENTION|MISSING_CITATION|WRONG_SOURCE|WRONG_VERSION|UNSUPPORTED_ANSWER Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Keep supplied precedence: unanswerable nonabstention first; answerable abstention; missing citation; wrong source; wrong version; exact valid citations PASS. Sources are synthetic health wording, not live advice.

#### Acceptance cases · expected, NOT_RUN

- CAP-49-CS-01: Input: answerable q1 expects health-A/v2; cites same. Expected: PASS.
- CAP-49-CS-02: Input: same source/v1. Expected: WRONG_VERSION.
- CAP-49-CS-03: Input: another source/v2. Expected: WRONG_SOURCE.
- CAP-49-CS-04: Input: unanswerable question answered with no citations. Expected: UNSUPPORTED_ANSWER, not missing-citation success.

#### Architecture/interview evidence delta

At most 2 minutes inside the existing docs/test-evidence allocation. On the primary’s existing evidence sheet, annotate the first failure branch: an unanswerable non-abstention is UNSUPPORTED_ANSWER. Keep the remaining precedence tree supplied and unchanged. Changed fact: Unanswerable non-abstention takes precedence over citation defects. The linked component contributes at most 1 changed fact and 2 nodes or 3 sequence messages. Primary and component together remain at most 5 minutes, 3 changed facts, 6 nodes or 10 messages; no new diagram or implementation task.

Alternative: One deterministic precedence category versus collecting multiple unordered failure labels. Interview question: Which reporting purpose favors a single stable category, and how would reversing the source/version checks change an observed failure? Sunday: no mandatory DSM or interview. This question is optional and may only reuse the existing individual-evidence allocation; it adds no minutes.
