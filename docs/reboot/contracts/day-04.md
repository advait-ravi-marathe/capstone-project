# Day 04 — 2026-10-23: Read one synthetic product

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Required course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section “Getting Started with Java Spring Framework - 5 - Key Questions about Spring”: “Step 13 - Exploring Java Bean vs POJO vs Spring Bean” (5:15; item 35020094). Optional later: section “Getting Started with Java Spring Framework - 4 - Playing with Spring Framework,” “Step 09 - Creating More Java Spring Beans in Spring Java Configuration File” (6:41; item 35020124). Catalog titles verified; playback not verified. Use the [worked collection-ownership guide](../teaching/day-04-immutable-values.md) before implementation. Total study remains 25 minutes: 5:15 lesson, 15:45 guide, 4:00 readiness.

## Learner seam

`ProductCatalogService`, `GET /api/products/homesecure`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Load the instructor resource [`homesecure-catalog.json`](../../../src/test/resources/assignments/reboot/homesecure-catalog.json), pinned by `HomeSecureCatalogFixtureInfrastructureTest`. `GET /api/products/homesecure` returns HTTP 200 with `productCode`, `displayName`, `productVersion`, `currency`, and the two `coverageOptions` exactly in fixture order (`BUILDING`, `BUILDING_CONTENTS`). Unknown code returns the Day 3 `RESOURCE_NOT_FOUND` envelope and 404. No other product exists. Returned collections are immutable to callers.

## Required edge cases

Known/unknown code, stable order, attempted mutation, exact fields and fixture values.

## Prerequisite and isolation

Day 3; test-only fixed product fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Design/readiness prompt (inside the existing evidence block):** After the worked example, explain who owns the source list, why a record alone does not prevent caller mutation, and one copying cost or mutable-element limitation. Do not add an architecture essay.

## Concept (25 minutes)

Record component references, collection aliasing, `List.copyOf`, and limits of shallow immutability. The bean lesson is background only; the worked guide teaches collection behavior before the product task.

## Evidence (10 minutes)

Run the source-add and source-remove reference observations, then record the actual capstone selector result and one ownership explanation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance. DSA notes remain separate.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day04ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 121 (D+3 from Day 1). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Who owns the source list? Why does a record alone not prevent caller mutation?

**Observable evidence:** Predict source mutation/removal, run the isolated reference, and explain what `List.copyOf` protects and does not protect.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A7](../interview-evidence-overlay.md#a7) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
