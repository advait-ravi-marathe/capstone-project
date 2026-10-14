# Day 011 · 24 Oct 2026 — Model catalog publication partition keys offline (150m)

**Date:** 2026-10-24 · **Phase:** 1 · **Primary ID:** `CAP-55` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `9a0be41f437d5e64eb612ee7800257c7358e07c5f652d4998f459b9abcd9b700`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain Kafka key locality and partition-order reasoning as an offline contract experiment; do not install a broker.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/catalog/api/CAP55Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP55Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-55/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-55.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-55/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-55/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-55` · primary · Day 011 · 24 Oct 2026 — Model catalog publication partition keys offline (150m)

Description SHA-256: `9a0be41f437d5e64eb612ee7800257c7358e07c5f652d4998f459b9abcd9b700`

**Practical task:** Retain Kafka key locality and partition-order reasoning as an offline contract experiment; do not install a broker.

Exact source description: [saved curriculum record `CAP-55`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/catalog/api/CAP55Seam.java
- src/test/java/capstone/coverstory/catalog/CAP55Test.java
- fixtures/coverstory/CAP-55/synthetic-v1.json
- docs/assignments/coverstory/CAP-55.md
- docs/evidence/coverstory/CAP-55/harness-self-check.json
- docs/evidence/coverstory/CAP-55/result.json

ProbeReport validatePartitionTrace(List<CatalogPublicationEnvelope>, List<RecordedObservation>) Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Instructor supplies pinned Kafka-shaped three-partition trace with SYNTHETIC source-version sequence metadata and a typed collision-safe key encoder. Observe same key/partition and rising offsets; different keys may collide. Output SIMULATED_CONTRACT_TRACE, never broker-run evidence. A later real isolated lab requires requirement/bottleneck, alternatives/cost/failure ADR and authorized local setup.

#### Acceptance cases · expected, NOT_RUN

- CAP-55-CS-01: Input: source-A/v1 sequences 1, 2, 3 at partition 0 offsets 10, 11, 12. Expected: valid per-key sequence/locality.
- CAP-55-CS-02: Input: source-B shares partition 0. Expected: valid; no claim distinct key implies distinct partition.
- CAP-55-CS-03: Input: blank source key. Expected: reject before producer spy.
- CAP-55-CS-04: Input: trace lacks metadata or claims actual broker run. Expected: evidence invalid; no invented offsets.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Partition data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-55/result.json. Annotate the supplied typed publication key → fake producer → recorded partition/offset → probe report flow. Record only these changed facts: One stable key retains partition locality in the supplied trace.; Different keys may share a partition.; The receipt is SIMULATED_CONTRACT_TRACE, not a live broker run..

Alternative: Aggregate-stable keys versus randomly changing keys; both differ from a global ordering guarantee. Interview question: Which assertion survives partition collisions, and what new evidence would be necessary before installing Kafka in this product? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
