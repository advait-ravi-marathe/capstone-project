# Day 004 · 17 Oct 2026 — Bound catalog review queue + citation evaluation (150m)

**Date:** 2026-10-17 · **Phase:** 1 · **Primary ID:** `CAP-46` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `8c973a56a47ad7a20ead18f9a0deca8421927333c4617c9c41d3aaf7ffaad12d`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Implement a bounded single-threaded FIFO for catalog-source review work, retaining Java collections and duplicate semantics.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/catalog/api/CAP46Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP46Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-46/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-46.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-46/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-46/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-46` · primary · Day 004 · 17 Oct 2026 — Bound catalog review queue + citation evaluation (150m)

Description SHA-256: `8c973a56a47ad7a20ead18f9a0deca8421927333c4617c9c41d3aaf7ffaad12d`

**Practical task:** Implement a bounded single-threaded FIFO for catalog-source review work, retaining Java collections and duplicate semantics.

Exact source description: [saved curriculum record `CAP-46`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

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
- src/main/java/capstone/coverstory/catalog/api/CAP46Seam.java
- src/test/java/capstone/coverstory/catalog/CAP46Test.java
- fixtures/coverstory/CAP-46/synthetic-v1.json
- docs/assignments/coverstory/CAP-46.md
- docs/evidence/coverstory/CAP-46/harness-self-check.json
- docs/evidence/coverstory/CAP-46/result.json

boolean enqueue(SourceVersionKey key); Optional<SourceVersionKey> poll(); int size(); capacity>0. Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Only SYNTHETIC source review requests; unique pending keys; duplicate/full false; poll removes identity so requeue is allowed. Queue is an in-process educational fixture, not a new runtime broker. CAP-47 shares this day for source-evaluation scoring.

#### Acceptance cases · expected, NOT_RUN

- CAP-46-CS-01: Input: capacity 2; enqueue health-A/v1, health-B/v1; poll twice. Expected: true, true; A then B; size 0.
- CAP-46-CS-02: Input: poll empty. Expected: Optional.empty.
- CAP-46-CS-03: Input: capacity 1 then second distinct key. Expected: false; first remains.
- CAP-46-CS-04: Input: duplicate, poll, requeue same key. Expected: true, false, A, true; no permanent source suppression.

#### Architecture/interview evidence delta

At most 3 minutes inside the existing docs/test-evidence allocation; at most 2 changed facts and either 4 nodes or 7 sequence messages. Do not draw a new full system or add implementation work. Primary and linked component share one evidence sheet: combined maximum 5 minutes, 3 changed facts, 6 nodes or 10 messages for the whole day. Use the instructor-supplied UML/data structure template and today’s existing test/receipt in docs/evidence/coverstory/CAP-46/result.json. Annotate the supplied bounded FIFO with a pending-key membership set and poll/remove boundary. Record only these changed facts: Capacity includes every pending key.; Duplicate enqueue adds nothing..

Alternative: Deque-only duplicate scans versus a deque plus a membership set. Interview question: What extra memory buys faster membership checks, and which operations must stay consistent even though this exercise is single-threaded? Use at most 2 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.


### `CAP-47` · linked_component · [Component of Day 004 / CAP-46] 17 Oct 2026 — Evaluate citation presence and abstention (included in shared 150m day; no extra time)

Description SHA-256: `71317eb68a043c0364a9c70e1416e8ff693c962b4a485952e4965738efe2014e`

**Practical task:** Measure citation presence and explicit abstention on a tiny health-wording fixture.

Exact source description: [saved curriculum record `CAP-47`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_47.py
- ai/coverstory/tests/test_cap_47.py
- fixtures/coverstory/CAP-47/synthetic-v1.json
- docs/assignments/coverstory/CAP-47.md
- docs/evidence/coverstory/CAP-47/harness-self-check.json
- docs/evidence/coverstory/CAP-47/result.json

evaluate(cases:list[EvalCase], answers:dict[str, Answer])->EvalReport Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Answer={abstained, citations}; each case has answerable flag and allowed source/version pairs. Supported means at least one allowed citation and no foreign citation; abstention contains no citations. Citation presence is not proof of insurance suitability or semantic correctness.

#### Acceptance cases · expected, NOT_RUN

- CAP-47-CS-01: Input: q1 answerable allowed w1/v1 answered with w1/v1; q2 unanswerable abstains with []. Expected: total 2, supported 1, correct_abstentions 1, unsupported 0.
- CAP-47-CS-02: Input: q1 has no citation or cites w9/v1. Expected: unsupported 1.
- CAP-47-CS-03: Input: missing answer id. Expected: ValueError; not correct abstention.
- CAP-47-CS-04: Input: abstained=true with citation. Expected: invalid/unsupported; no answer counted as correctly abstained.

#### Architecture/interview evidence delta

At most 2 minutes inside the existing docs/test-evidence allocation. On the primary’s existing evidence sheet, annotate the case-to-evaluation-result edge with the allowed source/version requirement. Use one case/result pair rather than draw another evaluator. Changed fact: A supported answer needs an allowed source/version citation. The linked component contributes at most 1 changed fact and 2 nodes or 3 sequence messages. Primary and component together remain at most 5 minutes, 3 changed facts, 6 nodes or 10 messages; no new diagram or implementation task.

Alternative: Count any nonempty citation list as support versus validate every citation against the allowed set. Interview question: Which failure would an answer-presence metric hide, and what additional guarantee is still missing after this citation check passes? Use at most 1 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.
