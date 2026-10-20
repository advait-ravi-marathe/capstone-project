# Day 10 — 2026-10-29: Persist selection batch atomically

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`SelectionService`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Persist pre-normalized batch in one PostgreSQL transaction; failure on any row leaves none of the batch. Successful batch assigns contiguous zero-based `selection_order` in input order. Isolated test never calls unfinished Day 1 policy.

## Required edge cases

Empty batch, two valid rows, induced second-row failure, pre-existing rows remain.

## Prerequisite and isolation

Days 8–9; canned normalized pairs and forced repository failure. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Transactions, atomicity, rollback [G].

## Evidence (10 minutes)

Database state after injected mid-batch failure. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day10ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 45 Jump Game II. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
