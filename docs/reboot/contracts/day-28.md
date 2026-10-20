# Day 28 — 2026-11-16: Milestone 2 browser-to-database acceptance

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`HomeSecureFlow` test. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

UI input → quote snapshot → owner selection → refreshed summary survives reload. Cover duplicate clicks, stale/expired conflict, other-user access and backend outage. No purchase or policy-issuance claim.

## Required edge cases

Success/reload; double submit; stale/expired; other user; outage.

## Prerequisite and isolation

Days 14–27; readiness checker reports gaps; real completed components and PostgreSQL. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

One request end to end, security and consistency boundaries.

## Evidence (10 minutes)

Automated report/screenshots and milestone rubric. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day28ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 1 Two Sum. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
