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

**System-design prompt (within the 25-minute concept block):** Show catalog ownership and how immutable product values reach the caller. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Java records, immutable collections, API vs domain models [J].

## Evidence (10 minutes)

Fixture read and mutation-resistance test. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day04ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 121 (D+3 from Day 1). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why separate API and domain representations? Can a caller mutate the returned catalog?

**Observable evidence:** Identify mutable-state ownership; show the mutation-resistance test; explain where immutability helps and what it costs.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A7](../interview-evidence-overlay.md#a7) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These sources motivate practice only; they do not establish question frequency or predict an interview.
