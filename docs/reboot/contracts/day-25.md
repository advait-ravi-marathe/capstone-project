# Day 25 — 2026-11-13: Handle concurrent repeated selection

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

persistence conflict mapping. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Two simultaneous authorized requests for the same pair create one row and both return HTTP 200 with the same selection representation; no unique-violation 500. Use two real transactions and a synchronization barrier; distinct from Day 1 in-batch dedup.

## Required edge cases

Concurrent same pair, different pair, final row count and both response bodies.

## Prerequisite and isolation

Days 8 and 10–12; canned authorized/current quote. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Race conditions, DB uniqueness, idempotency [E,G].

## Evidence (10 minutes)

Barrier-coordinated concurrency test; no sleeps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day25ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 205 Isomorphic Strings. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
