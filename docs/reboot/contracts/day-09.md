# Day 09 — 2026-10-28: Persist/read immutable quote snapshots

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`QuoteRepositoryAdapter`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Round trip preserves `NUMERIC(14,2)`, UTC timestamps and version ID; unknown `(journeyId, quoteVersionId)` returns explicit absence; reads require supplied journey scope; snapshots are insert-only through this adapter.

## Required edge cases

Round-trip precision, absent ID, wrong journey, update attempt.

## Prerequisite and isolation

Day 8; seeded rows, no quote-creation service. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

SQL queries, repository boundaries and ORM mapping [F].

## Evidence (10 minutes)

Disposable PostgreSQL test output. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day09ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 55 Jump Game. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
