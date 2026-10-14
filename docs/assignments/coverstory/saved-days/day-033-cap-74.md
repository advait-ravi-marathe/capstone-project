# Day 033 · 15 Nov 2026 — Expose owned mock payment attempt status (150m)

**Date:** 2026-11-15 · **Phase:** 1 · **Primary ID:** `CAP-74` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `118778164734c4f5f3e9baffc436b86fd78c4f77e1131270d840ecb1d8d4622b`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Map an authoritative owner-scoped payment-attempt snapshot into safe HTTP/UI state.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/payments/api/CAP74Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/payments/CAP74Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-74/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-74.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-74/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-74/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-74` · primary · Day 033 · 15 Nov 2026 — Expose owned mock payment attempt status (150m)

Description SHA-256: `118778164734c4f5f3e9baffc436b86fd78c4f77e1131270d840ecb1d8d4622b`

**Practical task:** Map an authoritative owner-scoped payment-attempt snapshot into safe HTTP/UI state.

Exact source description: [saved curriculum record `CAP-74`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/payments/api/CAP74Seam.java
- src/test/java/capstone/coverstory/payments/CAP74Test.java
- fixtures/coverstory/CAP-74/synthetic-v1.json
- docs/assignments/coverstory/CAP-74.md
- docs/evidence/coverstory/CAP-74/harness-self-check.json
- docs/evidence/coverstory/CAP-74/result.json

GET /api/v1/payment-attempts/{attemptId} -> {attemptId, state, updatedAt, requestId, nextAction}. Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Instructor supplies attempt repository, Clock, test principals, React status panel. PENDING/UNKNOWN/FAILED/SUCCEEDED are payment-only states; no policy active-cover claim. Anonymous 401, foreign/missing 404; never raw provider payload, secrets, answers. UNKNOWN nextAction RECONCILE; no next-attempt creation. Actual order creation is CAP-78.

#### Acceptance cases · expected, NOT_RUN

- CAP-74-CS-01: Input: A-owned attempt PENDING. Expected: 200 safe PENDING, show pending, not onboarding.
- CAP-74-CS-02: Input: A-owned attempt UNKNOWN. Expected: 200 UNKNOWN, RECONCILE, retain reference, no retry charge.
- CAP-74-CS-03: Input: B-owned/missing requested by A. Expected: identical 404.
- CAP-74-CS-04: Input: no principal or impossible internal state. Expected: 401 or sanitized 500 PAYMENT_STATE_INVALID, never misleading 200.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Payment state diagram template and today’s existing test/receipt in docs/evidence/coverstory/CAP-74/result.json. Annotate the supplied CREATED/PENDING/SUCCEEDED/FAILED/UNKNOWN states with the read-only status boundary. Record only these changed facts: A status GET never creates a new attempt or effect.; UNKNOWN requires same-reference reconciliation.; Foreign and absent attempt IDs serialize as the same404..

Alternative: Map every timeout to FAILED versus preserve UNKNOWN until authoritative evidence arrives. Interview question: How could an invented failure lead to duplicate payment, and why must a successful payment still not be described as issued cover? Sunday: no mandatory DSM or interview. This question is optional and may only reuse the existing individual-evidence allocation; it adds no minutes.
