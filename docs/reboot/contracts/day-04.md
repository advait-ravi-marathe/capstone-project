# Day 04 — 2026-10-23: Read one synthetic product

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`ProductCatalogService`, `GET /api/products/homesecure`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Load the instructor resource [`homesecure-catalog.json`](../../../src/test/resources/assignments/reboot/homesecure-catalog.json), pinned by `HomeSecureCatalogFixtureInfrastructureTest`. `GET /api/products/homesecure` returns HTTP 200 with `productCode`, `displayName`, `productVersion`, `currency`, and the two `coverageOptions` exactly in fixture order (`BUILDING`, `BUILDING_CONTENTS`). Unknown code returns the Day 3 `RESOURCE_NOT_FOUND` envelope and 404. No other product exists. Returned collections are immutable to callers.

## Required edge cases

Known/unknown code, stable order, attempted mutation, exact fields and fixture values.

## Prerequisite and isolation

Day 3; test-only fixed product fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Java records, immutable collections, API vs domain models [J].

## Evidence (10 minutes)

Fixture read and mutation-resistance test. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day04ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 26 Remove Duplicates from Sorted Array. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
