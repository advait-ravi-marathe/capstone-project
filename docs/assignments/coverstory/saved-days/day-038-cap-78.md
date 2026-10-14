# Day 038 · 20 Nov 2026 — Atomically record mock purchase and journey audit (150m)

**Date:** 2026-11-20 · **Phase:** 1 · **Primary ID:** `CAP-78` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `3b3fdec84286a94651e4216339704f95e4be7a3ef81bb61920584051b088605e`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Complete one SQL unit-of-work seam that records the validated mock purchase, authoritative audit and owned journey step together.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/payments/api/CAP78Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/payments/CAP78Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-78/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-78.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-78/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-78/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-78` · primary · Day 038 · 20 Nov 2026 — Atomically record mock purchase and journey audit (150m)

Description SHA-256: `3b3fdec84286a94651e4216339704f95e4be7a3ef81bb61920584051b088605e`

**Practical task:** Complete one SQL unit-of-work seam that records the validated mock purchase, authoritative audit and owned journey step together.

Exact source description: [saved curriculum record `CAP-78`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/payments/api/CAP78Seam.java
- src/test/java/capstone/coverstory/payments/CAP78Test.java
- fixtures/coverstory/CAP-78/synthetic-v1.json
- docs/assignments/coverstory/CAP-78.md
- docs/evidence/coverstory/CAP-78/harness-self-check.json
- docs/evidence/coverstory/CAP-78/result.json

createMockPaymentIntent(owner, journeyId, expectedJourneyVersion, validationId, acceptedTotalMinor, termsHash, idempotencyKey)->IntentResult; complete the supplied public-port unit-of-work seam only.

#### Concrete inputs and outputs

Revalidate the exact owned SYNTHETIC quote, accepted terms/amount and proposed five-minute validation expiry in one transaction. Create immutable mock_order, PaymentAttempt PENDING, Application AWAITING_PAYMENT and one local outbox intent, plus required transaction_audit_event and journey_step_event with the corresponding server-maintained journey version. Existing domain records remain the source of truth; audit is not a new ledger and journey contains no consumer answers. Encryption adapter has been verified before any required private payload write. External mock-provider calls stay outside SQL. Same actor/operation/resource/key hash replays; durable effect uniqueness outlives the proposed seven-day response cache. Analytics is neither a prerequisite nor evidence for purchase state.

#### Acceptance cases · expected, NOT_RUN

- CAP-78-CS-01: Input: A owns jA with the expected version and accepts quote-health-A-v1 at 100000 INR minor units with exact terms.. Expected: One order, one PENDING attempt, one AWAITING_PAYMENT application and one outbox intent, with required audit/step events and one journey advance; no ONBOARDING yet..
- CAP-78-CS-02: Input: Repeat same key and payload after a lost response.. Expected: Same operation/result; all logical counts remain 1; homepage GET resolves the authoritative journey without relying on browser events..
- CAP-78-CS-03: Input: Inject failure between any domain, outbox, audit, journey or step-event write.. Expected: Every new write rolls back; no partial financial or navigation evidence..
- CAP-78-CS-04: Input: Change accepted amount/terms under the same key, use a withdrawn source illustration, or submit a stale journey version.. Expected: 409/422 rejection with no new intent; no guessed payable total or silent acceptance..
- CAP-78-CS-05: Input: B knows jA or A validation UUID; analytics endpoint is unavailable.. Expected: B receives safe 404 and no decrypt/provider action. A valid authorized checkout still succeeds despite telemetry failure..
- CAP-78-CS-06: Input: Inspect persisted data and receipts for the supplied synthetic consumer secret marker.. Expected: Protected payload is encrypted and referenced only from its owning revision; plaintext marker absent from transaction audit, journey, analytics, URLs and logs..

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Purchase transaction sequence template and today’s existing test/receipt in docs/evidence/coverstory/CAP-78/result.json. Annotate the supplied quote review → SQL unit of work → committed intent → later mock-provider call sequence, with audit and journey participants inside the commit box. Record only these changed facts: Order/attempt/application intent, required audit and journey advance commit together.; External calls occur after the pending reference exists and outside the SQL transaction.; Duplicate keys replay the same effect; browser analytics is outside the correctness boundary..

Alternative: Call the provider inside the transaction versus persist a durable intent before an external call. Interview question: Which ambiguous outcomes still require reconciliation, and why must durable effect uniqueness outlive the seven-day response cache? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
