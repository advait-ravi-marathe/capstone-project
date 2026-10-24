# Day 25 — 2026-11-17: Handle concurrent repeated selection

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

Unique-key collision, PostgreSQL aborted transaction, rollback, and fresh-connection read.

## Evidence (10 minutes)

Barrier-coordinated concurrency test; no sleeps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day25ContractTest test`

This selector runs two concurrent calls against real PostgreSQL using a cyclic barrier and a fixed isolated schema. Both tests reach the explicit writer TODO. The current scaffold is service-level only; HTTP status/body and authorization wiring remain blocked and are not claimed. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Two app instances both see no existing row, then insert. Why can an in-memory lock or Day 1 dedup be insufficient?

**Observable evidence:** Draw the interleaving; run two transactions with a barrier; prove one row and the documented repeat result; distinguish JVM thread safety from cross-instance database correctness.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A7](../interview-evidence-overlay.md#a7) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
