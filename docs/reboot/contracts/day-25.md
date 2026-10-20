# Day 25 — 2026-11-13: Handle concurrent repeated selection

**State:** Real-PostgreSQL concurrent service contract is installed with a barrier and an isolated composite-key table. The writer is an explicit learner TODO; an HTTP endpoint/200-response integration remains open.

## Learner seam

persistence conflict mapping. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Two simultaneous authorized requests for the same pair create one row and both return HTTP 200 with the same selection representation; no unique-violation 500. Use two real transactions and a synchronization barrier; distinct from Day 1 in-batch dedup.

## Required edge cases

Concurrent same pair, different pair, final row count and both response bodies.

## Prerequisite and isolation

Days 8 and 10–12; canned authorized/current quote. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Draw two competing transactions and state the uniqueness/idempotency invariant; compare against a baseline. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Race conditions, DB uniqueness, idempotency [E,G].

## Evidence (10 minutes)

Barrier-coordinated concurrency test; no sleeps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day25ContractTest test`

This selector runs two concurrent calls against real PostgreSQL using a cyclic barrier and a fixed isolated schema. Both tests reach the explicit writer TODO. The current scaffold is service-level only; HTTP status/body and authorization wiring remain blocked and are not claimed. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 704 Binary Search — binary-search bridge (outside Top Interview 150). Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 25 (Handle concurrent repeated selection) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
