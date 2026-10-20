# Day 16 — 2026-11-04: Bound quote-history reads

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`QuoteHistoryQuery`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

`GET /api/journeys/{journeyId}/quotes` uses zero-based `page` (default 0), `size` (default 10, maximum 20), newest first by `createdAt` descending, then `quoteVersionId` ascending for ties. Empty/final pages are valid. Negative page, size below 1 or above 20 returns 400 `INVALID_REQUEST`.

## Required edge cases

Timestamp ties, page size 0/over 20, empty/final page, adjacent pages have no duplicate.

## Prerequisite and isolation

Days 9 and 12; seeded history. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Sorting, pagination cost and query complexity.

## Evidence (10 minutes)

Adjacent page fixture assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day16ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 6 Zigzag Conversion. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
