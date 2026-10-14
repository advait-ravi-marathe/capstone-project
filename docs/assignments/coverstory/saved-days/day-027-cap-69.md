# Day 027 · 9 Nov 2026 — Justify bounded local work queues (150m)

**Date:** 2026-11-09 · **Phase:** 1 · **Primary ID:** `CAP-69` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `9b7979bc3d99ec32793c234e3d53b3d4ff491016b2409c70def4f246a267da9a`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Use a deterministic catalog-export burst trace to compare direct rejection and bounded in-process queueing.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_69.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_69.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-69/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-69.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-69/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-69/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-69` · primary · Day 027 · 9 Nov 2026 — Justify bounded local work queues (150m)

Description SHA-256: `9b7979bc3d99ec32793c234e3d53b3d4ff491016b2409c70def4f246a267da9a`

**Practical task:** Use a deterministic catalog-export burst trace to compare direct rejection and bounded in-process queueing.

Exact source description: [saved curriculum record `CAP-69`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_69.py
- ai/coverstory/tests/test_cap_69.py
- fixtures/coverstory/CAP-69/synthetic-v1.json
- docs/assignments/coverstory/CAP-69.md
- docs/evidence/coverstory/CAP-69/harness-self-check.json
- docs/evidence/coverstory/CAP-69/result.json

summarize(LoadRun)->QueueDecisionEvidence Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

20 synthetic refresh tasks offered instantly, worker completes 2 per second, capacity 10 including in-flight; compute offered/completed/rejected, queue_max, p95, drain from provided trace. Keep baseline SQL and modular monolith; broker consideration requires product need or measurement, alternatives, costs/failures and an ADR. No installed queue or capacity claim.

#### Acceptance cases · expected, NOT_RUN

- CAP-69-CS-01: Input: 20 offered, capacity 10, 2 per second. Expected: accepted 10, rejected 10, completed 10, drain 5000 ms, queue maximum <=10.
- CAP-69-CS-02: Input: zero offered. Expected: counts 0, p95 null, drain 0.
- CAP-69-CS-03: Input: unbounded/zero capacity. Expected: INVALID_CONFIG.
- CAP-69-CS-04: Input: claim queue eliminates overload despite 10 rejections. Expected: evidence validator rejects claim.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Bounded queue data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-69/result.json. Annotate the supplied 20-offered burst → capacity 10 admission → 2-per-second worker → completed/rejected trace. Record only these changed facts: Capacity counts in-flight work.; Ten rejected tasks remain visible despite buffering.; Drain time is derived from the supplied trace, not asserted capacity..

Alternative: Direct rejection versus an in-process bounded queue versus a runtime broker with added operational cost. Interview question: Which delay does a queue redistribute rather than remove, and what missing product or measured evidence prevents a broker adoption decision? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
