# Day 031 · 13 Nov 2026 — Persist provider observations and audit atomically (150m)

**Date:** 2026-11-13 · **Phase:** 1 · **Primary ID:** `CAP-72` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `0c4320cdd7c22ddcc7007768a2e929745d2e6b83fa12a493498dc02f6d4b965d`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Preserve idempotent batch loading while every validated provider observation has durable append-only transaction audit evidence.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_72.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_72.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-72/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-72.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-72/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-72/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-72` · primary · Day 031 · 13 Nov 2026 — Persist provider observations and audit atomically (150m)

Description SHA-256: `0c4320cdd7c22ddcc7007768a2e929745d2e6b83fa12a493498dc02f6d4b965d`

**Practical task:** Preserve idempotent batch loading while every validated provider observation has durable append-only transaction audit evidence.

Exact source description: [saved curriculum record `CAP-72`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_72.py
- ai/coverstory/tests/test_cap_72.py
- fixtures/coverstory/CAP-72/synthetic-v1.json
- docs/assignments/coverstory/CAP-72.md
- docs/evidence/coverstory/CAP-72/harness-self-check.json
- docs/evidence/coverstory/CAP-72/result.json

load_provider_batch(batchId, validatedRows, unitOfWork)->LoadReport; manifest, provider_event and transaction_audit_event persist atomically through supplied Payments ports.

#### Concrete inputs and outputs

Use existing SYNTHETIC PaymentAttempt fixtures and canonical provider/event IDs. Same batch/ordered content returns ALREADY_LOADED; changed batch hash or same provider event ID with different validated payload conflicts. A validated initiated/pending/failed/unknown/reconciled/succeeded observation appends a transaction_audit_event with aggregate sequence, operation reference, safe provider reference, states, server-derived amount/currency when applicable and request_id. Audit refers to the actual attempt/journal effect and does not itself post money or unlock onboarding. Never copy raw provider payload, secrets or consumer form values. Unknown outcomes retain references for reconciliation; they are not rewritten as failures.

#### Acceptance cases · expected, NOT_RUN

- CAP-72-CS-01: Input: Batch b1 contains one FAILED and one UNKNOWN observation for prepared mock attempts.. Expected: Provider observations, manifest and two ordered audit records commit; UNKNOWN reference retained; no application transition or posted money effect..
- CAP-72-CS-02: Input: Repeat the exact same batch or provider event.. Expected: No duplicate logical effect or required audit record; original receipt is replayed..
- CAP-72-CS-03: Input: Reuse batch ID with changed content, or provider event ID with a changed amount.. Expected: BATCH_CONFLICT or EVENT_HASH_CONFLICT; original data/history unchanged..
- CAP-72-CS-04: Input: Inject failure after any provider, audit or manifest write.. Expected: Whole batch rolls back; no half-audit or falsely completed manifest..
- CAP-72-CS-05: Input: A later reconciliation succeeds for a previously UNKNOWN attempt.. Expected: Append a new observation referencing the same operation; retain UNKNOWN history. Money totals use the owning module’s effect record once, not the number of audit rows..

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied ER/atomic audit boundary template and today’s existing test/receipt in docs/evidence/coverstory/CAP-72/result.json. Annotate the supplied batch manifest, provider_event, PaymentAttempt reference and transaction_audit_event relationship with a single write transaction. Record only these changed facts: Provider identity plus event ID and payload hash control deduplication.; Audit observations reference the actual attempt/effect rather than become money.; Manifest/provider/audit writes roll back together..

Alternative: Overwrite UNKNOWN with success versus append a reconciliation observation linked to the same operation. Interview question: Why must totals count the money effect once rather than sum audit observations, and what survives when recording a provider result rolls back? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
