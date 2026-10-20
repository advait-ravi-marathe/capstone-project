# Day 11 — 2026-10-30: Reject stale quote selection

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`QuoteFreshnessPolicy`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Only the journey’s current unexpired version can be newly selected. Expired-at-now returns HTTP 409 `QUOTE_EXPIRED`; superseded returns HTTP 409 `QUOTE_VERSION_STALE`; quote from another journey returns HTTP 404 `RESOURCE_NOT_FOUND`. Each rejection writes zero rows.

## Required edge cases

Current, expired-at-now, superseded, wrong journey, and no-write checks.

## Prerequisite and isolation

Days 7 and 9–10; fixed current-version metadata and Clock. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Optimistic concurrency vs stale reads [G].

## Evidence (10 minutes)

Boundary and stale-version negative tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day11ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 13 Roman to Integer. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
