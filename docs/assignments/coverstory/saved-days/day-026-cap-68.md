# Day 026 · 8 Nov 2026 — Expose commit-before-response uncertainty (150m)

**Date:** 2026-11-08 · **Phase:** 1 · **Primary ID:** `CAP-68` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `81db5bcc6929a560cf504a5bb728543d063fc9ff2ceadbe76b98ea93fafe8183`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Demonstrate the durable-shortlist/response-loss gap and a local notification-publish gap without claiming either is repaired by retrying money operations.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/shortlist/api/CAP68Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/shortlist/CAP68Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-68/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-68.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-68/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-68/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-68` · primary · Day 026 · 8 Nov 2026 — Expose commit-before-response uncertainty (150m)

Description SHA-256: `81db5bcc6929a560cf504a5bb728543d063fc9ff2ceadbe76b98ea93fafe8183`

**Practical task:** Demonstrate the durable-shortlist/response-loss gap and a local notification-publish gap without claiming either is repaired by retrying money operations.

Exact source description: [saved curriculum record `CAP-68`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/shortlist/api/CAP68Seam.java
- src/test/java/capstone/coverstory/shortlist/CAP68Test.java
- fixtures/coverstory/CAP-68/synthetic-v1.json
- docs/assignments/coverstory/CAP-68.md
- docs/evidence/coverstory/CAP-68/harness-self-check.json
- docs/evidence/coverstory/CAP-68/result.json

crashAfterShortlistCommitBeforeResponse() and provided offline publication hook -> GapReport Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

The supplied shortlist/journey command uses the established atomic domain+audit+journey_step_event boundary from CAP-60. A lost HTTP response never implies lost transaction history: GET/replay verifies the same durable operation and projection. Missing optional client analytics remains a separate best-effort gap, never a missing financial audit record.

Instructor supplies A-owned shortlist commit and crash hooks, GET lookup, same-key stored-result recovery. Learner proves DB commit survives lost HTTP response, client state UNKNOWN until GET or replay. Separate fake notification port can remain undelivered; no broker. CAP-78 later closes required durable-work atomicity, not this day.

#### Acceptance cases · expected, NOT_RUN

- CAP-68-CS-01: Input: normal shortlist commit plus response. Expected: one row, one 201, known version.
- CAP-68-CS-02: Input: crash after commit before response. Expected: one durable row; client UNKNOWN; no inferred failure.
- CAP-68-CS-03: Input: same key retry/owned GET. Expected: original result/known version; no second shortlist.
- CAP-68-CS-04: Input: fake publication missing despite SQL row. Expected: report gap; DB receipt cannot prove notification delivery.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Commit/response-loss sequence template and today’s existing test/receipt in docs/evidence/coverstory/CAP-68/result.json. Annotate the supplied authoritative commit → lost HTTP response → same-key retry or owner GET → recovered state sequence. Record only these changed facts: Domain, audit and journey events stay committed even when the response is lost.; The existing operation reference is reused.; Missing best-effort analytics is separate from missing durable audit..

Alternative: Create a new operation after timeout versus resolve uncertainty by GET or the original idempotency key. Interview question: Which evidence proves the commit and which proves response delivery, and why can a missing browser event not justify another purchase? Sunday: no mandatory DSM or interview. This question is optional and may only reuse the existing individual-evidence allocation; it adds no minutes.
