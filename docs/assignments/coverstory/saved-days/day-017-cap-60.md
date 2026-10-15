# Day 017 · 30 Oct 2026 — Race authoritative journey transitions safely (150m)

**Date:** 2026-10-30 · **Phase:** 1 · **Primary ID:** `CAP-60` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `142636990a8cfc2d7a5c2524d9c4f3e720e31c353c559dabb145ca4bc6119d4b`

This guide carries the saved card contract as source evidence. The instructor reference adapter and race harness now run; planned paths remain specifications, the timebox is unmeasured, and learner work has not passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Use deterministic real-SQL races to prove idempotency, optimistic journey versions and atomic authoritative audit boundaries.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/api/CAP60Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/CAP60Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-60/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-60.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-60/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-60/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-60` · primary · Day 017 · 30 Oct 2026 — Race authoritative journey transitions safely (150m)

Description SHA-256: `142636990a8cfc2d7a5c2524d9c4f3e720e31c353c559dabb145ca4bc6119d4b`

**Practical task:** Use deterministic real-SQL races to prove idempotency, optimistic journey versions and atomic authoritative audit boundaries.

Exact source description: [saved curriculum record `CAP-60`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/application/api/CAP60Seam.java
- src/test/java/capstone/coverstory/application/CAP60Test.java
- fixtures/coverstory/CAP-60/synthetic-v1.json
- docs/assignments/coverstory/CAP-60.md
- docs/evidence/coverstory/CAP-60/harness-self-check.json
- docs/evidence/coverstory/CAP-60/result.json

concurrentAdvanceSameKey() using supplied domain command, unit-of-work and two PostgreSQL connections; learner writes the bounded race/invariant test only.

#### Concrete inputs and outputs

journey version 4 is owned by A and references current domain records. Supplied server command carries expected domain/journey versions and actor+operation+resource+Idempotency-Key payload hash. Domain mutation, transaction_audit_event, journey version 5 and journey_step_event commit together. Unique(journey_id, sequence) and unique(journey_id, causation_id, event_code) prevent duplicate advances. Two independent connections start at a barrier; waits <=5 seconds and ten clean-key repetitions. This is authoritative command testing, not a browser analytics endpoint.

#### Acceptance cases · expected, NOT_RUN

- CAP-60-CS-01: Input: Two same-key/same-payload A commands released at one barrier.. Expected: One logical domain effect, one required audit event and one journey advance; duplicate returns stored result..
- CAP-60-CS-02: Input: Two different valid commands both expect journey version 4.. Expected: One commits version 5 with one step event; the other returns 409 JOURNEY_VERSION_CONFLICT; no overwrite..
- CAP-60-CS-03: Input: Inject failure after domain, audit, journey projection or step-event write.. Expected: Each transaction rolls back every associated write; no partial business/audit/navigation state..
- CAP-60-CS-04: Input: Same key with changed quote/revision payload, or B with jA UUID.. Expected: 409 payload conflict or safe 404 owner denial; no second effect and decrypt calls0 for B..
- CAP-60-CS-05: Input: Worker never reaches the barrier; late PAGE_VIEW arrives afterward.. Expected: Race test fails within 5 seconds; telemetry never repairs or changes the authoritative journey version..

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Concurrent transaction sequence template and today’s existing test/receipt in docs/evidence/coverstory/CAP-60/result.json. Annotate the supplied two-connection barrier → version check → atomic domain/audit/journey commit sequence. Record only these changed facts: Same key and payload replays one logical result.; Two distinct commands expecting version 4 yield one version 5 winner.; Every injected write-boundary failure rolls back the whole authoritative unit..

Alternative: Last-write-wins replacement versus expected-version concurrency plus durable operation identity. Interview question: Why are an idempotent duplicate and a stale-version conflict different, and how would checking only the final journey row miss a broken audit boundary? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
