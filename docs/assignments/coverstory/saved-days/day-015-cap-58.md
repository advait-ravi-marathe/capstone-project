# Day 015 · 28 Oct 2026 — Validate minimal screen analytics events (150m)

**Date:** 2026-10-28 · **Phase:** 1 · **Primary ID:** `CAP-58` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `2f1927c6a8de5f6fe0ed67ea2b2bb1f1a898bb5618c1329fb98e7da9d6687545`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain Java schema/length/UTF-8 boundary validation against minimal page-load and click telemetry, without accepting consumer values.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/api/CAP58Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/CAP58Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-58/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-58.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-58/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-58/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-58` · primary · Day 015 · 28 Oct 2026 — Validate minimal screen analytics events (150m)

Description SHA-256: `2f1927c6a8de5f6fe0ed67ea2b2bb1f1a898bb5618c1329fb98e7da9d6687545`

**Practical task:** Retain Java schema/length/UTF-8 boundary validation against minimal page-load and click telemetry, without accepting consumer values.

Exact source description: [saved curriculum record `CAP-58`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/application/api/CAP58Seam.java
- src/test/java/capstone/coverstory/application/CAP58Test.java
- fixtures/coverstory/CAP-58/synthetic-v1.json
- docs/assignments/coverstory/CAP-58.md
- docs/evidence/coverstory/CAP-58/harness-self-check.json
- docs/evidence/coverstory/CAP-58/result.json

ValidationResult validateAnalyticsBatch(byte[] utf8Body, TrustedAnalyticsContext context); instructor supplies JSON parser, policy check, clock and versioned allowlists.

#### Concrete inputs and outputs

PAGE_VIEW requires target_id to be absent. CONTROL_CLICK requires a target from the versioned allowlist, such as compare_plan or save_draft. Allowed initial page IDs include health_discovery, compare, quote_review, home, checkout, onboarding and final_review. optional_duration_ms is an integer 0..300000, with booleans rejected; client_time is absent or an aware timestamp and remains untrusted. Unknown enum values are rejected, not silently mapped.

POST /api/v1/analytics/events accepts PAGE_VIEW or CONTROL_CLICK, schema_version 1, event_id UUID, pseudonymous_session_id and allowlisted page_id/step_code/target_id; optional bounded duration and client_time. At most 10 events and 8192 UTF-8 bytes. Reject unknown fields rather than retaining them. page_id is a supplied allowlisted string of at most 64 characters; client labels confer no authority. Validate private journey links against the authenticated owner. 400 schema error, 413 body overflow, 429 rate limit; policy-disallowed arrivals are discarded without payload storage. The old 4096-byte event payload is historical; the same Java byte-boundary objective now uses the reviewed 8192-byte telemetry contract.

#### Acceptance cases · expected, NOT_RUN

- CAP-58-CS-01: Input: One permitted PAGE_VIEW with valid UUIDs and allowlisted health page; policy allows collection.. Expected: Valid sanitized event; server receive time is assigned independently; no journey state change..
- CAP-58-CS-02: Input: Exactly 10 valid events in a valid JSON body padded with whitespace to 8192 bytes; then 8193 bytes or 11 events.. Expected: The exact limits pass; oversized body 413; an 11-event batch 400. Byte counts use UTF-8, not Java character count..
- CAP-58-CS-03: Input: A byte-boundary probe containing 2049 four-byte code points.. Expected: 8196 bytes exceeds the body cap and returns 413; it is never accepted as schema-valid merely because character count is small..
- CAP-58-CS-04: Input: CONTROL_CLICK includes amountMinor, email, healthAnswer, rawUrl, token or arbitrary properties with a synthetic secret marker.. Expected: 400; zero marker bytes persisted or logged; safe error contains field code only..
- CAP-58-CS-05: Input: Anonymous or B-linked event claims A journey UUID.. Expected: Unauthorized link rejected before private serialization; no journey row or transition created..
- CAP-58-CS-06: Input: Purpose/consent policy disallows collection.. Expected: Client emits nothing; defensive server arrival gets 202 DISCARDED_BY_POLICY and no payload persistence..
- CAP-58-CS-07: Input: PAGE_VIEW contains target_id; CONTROL_CLICK names an unknown target; duration is true or 300001.. Expected: 400 with a stable field code; no payload persistence. Durations 0 and 300000 are valid integer boundaries..
- CAP-58-CS-08: Input: Send PAGE_VIEW analytics-e1 twice with the exact same allowlisted payload; then reuse analytics-e1 with a different valid page_id.. Expected: The supplied ingestion adapter returns202 RECEIVED, then202 DUPLICATE with one telemetry row; changed valid payload returns409 EVENT_ID_CONFLICT. No domain effect or journey advance..
- CAP-58-CS-09: Input: Deliver valid events with reversed client timestamps; force429/unavailable responses while the supplied browser queue reaches20 events and3 attempts.. Expected: Server receive time is retained separately; authoritative order is unchanged. Queue/retries stop at their caps and may drop telemetry; essential comparison/checkout/resume is never blocked..

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Telemetry data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-58/result.json. Annotate the supplied purpose gate → bounded browser queue → schema/owner validator → receipt flow, outside authoritative domain transitions. Record only these changed facts: Only versioned allowlisted metadata passes the 8 KiB / 10-event contract.; Event UUID deduplication has no business-state authority.; Loss or retry exhaustion cannot block checkout or resume..

Alternative: Capture arbitrary DOM/form properties versus finite page/control identifiers with explicit policy discard. Interview question: Why is same UUID with a different valid page a 409 while a PAGE_VIEW target field is 400, and why must neither receipt advance a journey? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
