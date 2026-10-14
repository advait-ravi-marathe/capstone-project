# Day 034 · 16 Nov 2026 — Classify mock payment ingestion failures (150m)

**Date:** 2026-11-16 · **Phase:** 1 · **Primary ID:** `CAP-75` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `f4906fca5a1b98ced09fe2430b472af3f13bc39881120c9a07ef3ec68c0731dd`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Turn a provider-ingestion failure into a reproducible safe diagnosis and one bounded classification repair.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_75.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_75.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-75/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-75.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-75/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-75/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-75` · primary · Day 034 · 16 Nov 2026 — Classify mock payment ingestion failures (150m)

Description SHA-256: `f4906fca5a1b98ced09fe2430b472af3f13bc39881120c9a07ef3ec68c0731dd`

**Practical task:** Turn a provider-ingestion failure into a reproducible safe diagnosis and one bounded classification repair.

Exact source description: [saved curriculum record `CAP-75`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_75.py
- ai/coverstory/tests/test_cap_75.py
- fixtures/coverstory/CAP-75/synthetic-v1.json
- docs/assignments/coverstory/CAP-75.md
- docs/evidence/coverstory/CAP-75/harness-self-check.json
- docs/evidence/coverstory/CAP-75/result.json

classify_provider_failure(run)->{stage, code, retryable, evidence_ids} Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

MALFORMED_JSON parse non-retryable; DB_TIMEOUT load retryable by same batch/reference; EVENT_HASH_CONFLICT non-retryable. UNKNOWN transport outcome requires reconciliation, not creating a new payment. Learner repairs one seeded classification bug; diagnostics reference batch/line IDs only.

#### Acceptance cases · expected, NOT_RUN

- CAP-75-CS-01: Input: mock batch b1, line 2 invalid JSON. Expected: PARSE, MALFORMED_JSON, false, b1:2.
- CAP-75-CS-02: Input: SQL timeout before known commit. Expected: LOAD, DB_TIMEOUT, true; same batch replay/lookup.
- CAP-75-CS-03: Input: same provider eventID, different hash. Expected: IDEMPOTENCY, EVENT_HASH_CONFLICT, false.
- CAP-75-CS-04: Input: unknown exception with synthetic secret. Expected: UNKNOWN, false, sanitized evidence, no exception text.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Failure classification flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-75/result.json. Annotate the supplied parse/load/idempotency stage → safe failure code → bounded recovery decision. Record only these changed facts: Malformed input requires correction rather than retry.; A transient database failure uses the same batch/reference.; An unknown outcome never authorizes a new money operation..

Alternative: Retry every exception versus classify the failure using stage and durable outcome evidence. Interview question: Which secret-bearing exception detail must be excluded, and why is retryable transport not the same as safe repetition of a financial effect? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
