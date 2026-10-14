# Day 001 · 14 Oct 2026 — Preserve health shortlist order + source citations (150m)

**Date:** 2026-10-14 · **Phase:** 1 · **Primary ID:** `CAP-40` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `b9c9ea8e9a53cde75b4a67064f1ea1fa5f3ba0126e5788063007f1235a4687a0`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Choose composite Java collection keys for device-local health selections, preserving first-seen order; retrieve exact source excerpts in the linked component.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/shortlist/api/CAP40Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/shortlist/CAP40Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-40/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-40.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-40/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-40/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-40` · primary · Day 001 · 14 Oct 2026 — Preserve health shortlist order + source citations (150m)

Description SHA-256: `b9c9ea8e9a53cde75b4a67064f1ea1fa5f3ba0126e5788063007f1235a4687a0`

**Practical task:** Choose composite Java collection keys for device-local health selections, preserving first-seen order; retrieve exact source excerpts in the linked component.

Exact source description: [saved curriculum record `CAP-40`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

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
- src/main/java/capstone/coverstory/shortlist/api/CAP40Seam.java
- src/test/java/capstone/coverstory/shortlist/CAP40Test.java
- fixtures/coverstory/CAP-40/synthetic-v1.json
- docs/assignments/coverstory/CAP-40.md
- docs/evidence/coverstory/CAP-40/harness-self-check.json
- docs/evidence/coverstory/CAP-40/result.json

List<SelectionKey> uniqueSelections(List<SelectionInput> input); SelectionKey(deviceScope, catalogVersionId). Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Input is an immutable list of SYNTHETIC device-scoped catalog references; return unique exact pairs in encounter order. Reject null list/elements and blank key parts without mutation. This is local identity, not authentication or permission to read a saved shortlist.

#### Acceptance cases · expected, NOT_RUN

- CAP-40-CS-01: Input: [(device-A, health-A-v1), (device-A, health-B-v1), (device-A, health-A-v1)]. Expected: A-v1, B-v1 in that order; original list unchanged.
- CAP-40-CS-02: Input: []. Expected: [].
- CAP-40-CS-03: Input: same version in device-A and device-B. Expected: Two independent keys.
- CAP-40-CS-04: Input: blank catalogVersionId. Expected: IllegalArgumentException and no partial result.

#### Architecture/interview evidence delta

At most 3 minutes inside the existing docs/test-evidence allocation; at most 2 changed facts and either 4 nodes or 7 sequence messages. Do not draw a new full system or add implementation work. Primary and linked component share one evidence sheet: combined maximum 5 minutes, 3 changed facts, 6 nodes or 10 messages for the whole day. Use the instructor-supplied UML/data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-40/result.json. Annotate the supplied SelectionKey(deviceScope, catalogVersionId) → membership check → ordered output sketch. Record only these changed facts: Identity includes both key components.; First encounter determines output position..

Alternative: LinkedHashMap versus HashSet plus a separate ordered output list; a plain HashSet alone does not promise encounter order. Interview question: Which operations preserve the first occurrence, what is their expected time/space cost, and why is device scope still not authentication? Use at most 2 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.


### `CAP-41` · linked_component · [Component of Day 001 / CAP-40] 14 Oct 2026 — Retrieve exact synthetic health wording (included in shared 150m day; no extra time)

Description SHA-256: `2a926df96da08ee481d00376e62d843455cd3608f954619989399152d27f0d24`

**Practical task:** Retrieve traceable public health wording without a model call.

Exact source description: [saved curriculum record `CAP-41`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_41.py
- ai/coverstory/tests/test_cap_41.py
- fixtures/coverstory/CAP-41/synthetic-v1.json
- docs/assignments/coverstory/CAP-41.md
- docs/evidence/coverstory/CAP-41/harness-self-check.json
- docs/evidence/coverstory/CAP-41/result.json

retrieve(query: str, documents: list[PublicWording], k: int) -> list[Hit] Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Instructor supplies lowercase whitespace tokenizer and public-only SYNTHETIC wording. Score distinct query tokens; score>0; sort descending score then source_id; k=1..3; blank query returns []. Preserve source_id, version, evidenceStatus, exact excerpt.

#### Acceptance cases · expected, NOT_RUN

- CAP-41-CS-01: Input: query='waiting period'; w-health-A/v1='waiting period'; w-health-B/v1='waiting'; k=2. Expected: A score 2 then B score 1 with exact SYNTHETIC citations.
- CAP-41-CS-02: Input: query='guaranteed acceptance' with no matching text. Expected: []; no advice or invented citation.
- CAP-41-CS-03: Input: k=0 or missing source version. Expected: ValueError; no partial answer.
- CAP-41-CS-04: Input: reverse input documents. Expected: Identical ordered hits.

#### Architecture/interview evidence delta

At most 2 minutes inside the existing docs/test-evidence allocation. On the primary’s existing evidence sheet, annotate only the citation/source-version edge with the exact SYNTHETIC source and excerpt pair. Use at most two labeled fields or nodes; do not draw the full retrieval pipeline. Changed fact: Only public, applicable SYNTHETIC wording enters this fixture. The linked component contributes at most 1 changed fact and 2 nodes or 3 sequence messages. Primary and component together remain at most 5 minutes, 3 changed facts, 6 nodes or 10 messages; no new diagram or implementation task.

Alternative: Deterministic extractive ranking versus a model-generated answer with broader cost and grounding risks. Interview question: Why can a higher token score justify ordering these fixtures but not confidence, coverage inclusion or insurance advice? Use at most 1 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task. Primary and component share a maximum 3-minute interview/reflection contribution for the whole day.
