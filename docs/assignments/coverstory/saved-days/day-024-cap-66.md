# Day 024 · 6 Nov 2026 — Compare consumer groups using recorded traces (150m)

**Date:** 2026-11-06 · **Phase:** 1 · **Primary ID:** `CAP-66` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `31bffe7497d5d837e4890f5eee6c63a4d9cbf337cc798bff4c27c115d22473a8`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain consumer-group assignment, committed-next-offset and redelivery learning using bounded offline catalog refresh traces.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/catalog/api/CAP66Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/catalog/CAP66Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-66/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-66.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-66/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-66/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-66` · primary · Day 024 · 6 Nov 2026 — Compare consumer groups using recorded traces (150m)

Description SHA-256: `31bffe7497d5d837e4890f5eee6c63a4d9cbf337cc798bff4c27c115d22473a8`

**Practical task:** Retain consumer-group assignment, committed-next-offset and redelivery learning using bounded offline catalog refresh traces.

Exact source description: [saved curriculum record `CAP-66`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/catalog/api/CAP66Seam.java
- src/test/java/capstone/coverstory/catalog/CAP66Test.java
- fixtures/coverstory/CAP-66/synthetic-v1.json
- docs/assignments/coverstory/CAP-66.md
- docs/evidence/coverstory/CAP-66/harness-self-check.json
- docs/evidence/coverstory/CAP-66/result.json

GroupReport observe(recordedProbeSnapshots, expectedVersions) Implement only this bounded seam and its focused cases; do not edit fixture oracle, authorization policy or dependencies to force a pass. No completed implementation is supplied.

#### Concrete inputs and outputs

Instructor supplies three-partition/six-publication fixture; group G with 2 members, group H with 1 member. G union and H union independently cover 6; stable assignment disjoint; rebalance ownership may change. These are simulated contract checks, not live-broker observations or runtime architecture changes.

#### Acceptance cases · expected, NOT_RUN

- CAP-66-CS-01: Input: G1/G2 stable assignment and H1 complete six records. Expected: G union 6, H union 6; disjoint G in snapshot.
- CAP-66-CS-02: Input: fourth member with 3 partitions. Expected: idle member allowed.
- CAP-66-CS-03: Input: restart committed through offset n. Expected: next read n+1 per fixture config.
- CAP-66-CS-04: Input: restart before commit repeats record. Expected: redelivery reported; no exactly-once claim.

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Consumer-group assignment diagram template and today’s existing test/receipt in docs/evidence/coverstory/CAP-66/result.json. Annotate the supplied three partitions and the G1/G2/H1 assignment snapshot. Record only these changed facts: Within group G a stable partition assignment has one active member.; Group H independently observes the same publication set.; Restart reads from the committed next offset and can redeliver before commit..

Alternative: One group for independent subscribers versus separate groups; this differs from adding members for work sharing. Interview question: Why can a fourth group member be idle, and why does a rebalance invalidate permanent member-to-partition assumptions? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
