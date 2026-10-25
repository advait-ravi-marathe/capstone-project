# Day 25 — 2026-11-19: Handle concurrent repeated selection

**State:** Real-PostgreSQL concurrent service contract is installed with a barrier and an isolated composite-key table. The writer is an explicit learner TODO; an HTTP endpoint/200-response integration remains open.

**Course/concept bridge:** No exact verified lecture covers this race. Read the named PostgreSQL transactions, Read Committed, and `INSERT ... ON CONFLICT` sections, then run the [Day 25 guide](../teaching/day-25-unique-race.md#two-transaction-trace) within the existing 25-minute block. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

persistence conflict mapping. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Two simultaneous authorized requests for the same pair create one row and both return HTTP 200 with the same selection representation; no unique-violation 500. Use two real transactions and a synchronization barrier; distinct from Day 1 in-batch dedup.

## Required edge cases

Concurrent same pair, different pair, final row count and both response bodies.

## Prerequisite and isolation

Days 8 and 10–12; canned authorized/current quote. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Draw both transaction lanes and explain why the losing transaction must rollback before a fresh read.

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-25](<../assistant-architecture-notes-15-70.md#day-25>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Unique-key collision, PostgreSQL aborted transaction, rollback, and fresh-connection read.

## Evidence (10 minutes)

Barrier-coordinated concurrency test; no sleeps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day25ContractTest test`

This selector runs two concurrent calls against real PostgreSQL using a cyclic barrier and a fixed isolated schema. Both tests reach the explicit writer TODO. The current scaffold is service-level only; HTTP status/body and authorization wiring remain blocked and are not claimed. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
