# Day 27 — 2026-11-15: Fix one evidence-backed regression

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

targeted learner test and small production fix. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Select highest-value observed failure; reproduce first, then fix and rerun targeted plus affected contracts. If none exists, add one missing boundary test. Do not invent a defect or add a feature.

## Required edge cases

Before state must show reproducible failure or missing boundary; after targeted and affected checks.

## Prerequisite and isolation

Relevant failed day; saved fixture reproducer. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Debugging hypothesis, regression prevention, scope control.

## Evidence (10 minutes)

Before/after assertion and affected-suite result. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day27ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 242 Valid Anagram. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
