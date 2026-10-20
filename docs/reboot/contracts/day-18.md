# Day 18 — 2026-11-06: Capture quote input

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`QuoteForm.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Required inputs are occupancy, rebuild amount in INR and one Day 4 coverage code; each has an accessible label. Valid synthetic data submits once; invalid input has field feedback; pending blocks duplicate submit. Server remains authoritative and repeats Day 5 bounds only for feedback.

## Required edge cases

Keyboard submit, invalid input, pending double click, success and rejection.

## Prerequisite and isolation

Day 5 request contract; fake submit promise. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Controlled inputs, derived state, client/server validation [K].

## Evidence (10 minutes)

Keyboard, validation and pending-state component tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day18ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 392 Is Subsequence. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
