# Day 036 · 18 Nov 2026 — Bound safe reconciliation reads (150m)

**Date:** 2026-11-18 · **Phase:** 1 · **Primary ID:** `CAP-76` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `7ac6d1c0e1e7fde8fda8708976adb08417c5b8bce157c85022762aa5bd1ff4a5`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain attempt/time-budget retry policy for idempotent mock-provider status GETs only.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/payments/api/CAP76Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/payments/CAP76Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-76/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-76.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-76/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-76/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-76` · primary · Day 036 · 18 Nov 2026 — Bound safe reconciliation reads (150m)

Description SHA-256: `7ac6d1c0e1e7fde8fda8708976adb08417c5b8bce157c85022762aa5bd1ff4a5`

**Practical task:** Retain attempt/time-budget retry policy for idempotent mock-provider status GETs only.

Exact source description: [saved curriculum record `CAP-76`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/payments/api/CAP76Seam.java
- src/test/java/capstone/coverstory/payments/CAP76Test.java
- fixtures/coverstory/CAP-76/synthetic-v1.json
- docs/assignments/coverstory/CAP-76.md
- docs/evidence/coverstory/CAP-76/harness-self-check.json
- docs/evidence/coverstory/CAP-76/result.json

RetryDecision next(attemptNumber, httpStatus, elapsed, parsedRetryAfter) Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Attempts 1..3; 2xx known success is evaluated before elapsed-time exhaustion; 408/429/5xx transient, other 4xx permanent. Backoff 100/200 ms; elapsed plus delay strictly<1000ms. Valid Retry-After is honored only if within the remaining budget, never earlier; long delays defer/stop. A timeout of a payment create/capture is UNKNOWN and outside this automatic retry policy.

#### Acceptance cases · expected, NOT_RUN

- CAP-76-CS-01: Input: attempt 1, status 503, elapsed 0, no header. Expected: RETRY_AFTER 100 ms.
- CAP-76-CS-02: Input: attempt 2, 429, elapsed 100 ms, no header. Expected: RETRY_AFTER 200 ms.
- CAP-76-CS-03: Input: attempt 3 transient or elapsed 900 + delay 200 ms. Expected: STOP_EXHAUSTED.
- CAP-76-CS-04: Input: attempt 1, 403. Expected: STOP_PERMANENT.
- CAP-76-CS-05: Input: 2xx received after the deadline. Expected: STOP_SUCCESS, known response not erased.
- CAP-76-CS-06: Input: possible capture timeout or Retry-After 31 seconds. Expected: UNKNOWN_REQUIRES_RECONCILIATION for money; defer for status read; no new charge.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Retry state machine template and today’s existing test/receipt in docs/evidence/coverstory/CAP-76/result.json. Annotate the supplied status result → known success/permanent failure/transient branch → attempt/time budget decision. Record only these changed facts: A known 2xx result is success even if the deadline has elapsed.; A transient retry needs both attempts remaining and delay strictly inside the time budget.; Possible-charge uncertainty is outside the automatic read-retry policy..

Alternative: Attempt-count-only retry versus combined attempt, elapsed-time and Retry-After bounds. Interview question: Why must elapsed 900 ms plus a 200 ms wait stop, and why can a provider status GET be retried when a possibly committed charge cannot simply be recreated? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
