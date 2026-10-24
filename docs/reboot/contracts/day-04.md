# Day 04 — 2026-10-27: Read one synthetic product

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



## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Return an immutable copy of the catalog collection and retain the supplied immutable product values. This protects collection membership without introducing a new mutable object graph.](../assistant-architecture-notes.md#day-4-immutable-catalog-boundary). No capstone design proposal or post-hoc architecture justification is assigned.

Record component references, collection aliasing, `List.copyOf`, and limits of shallow immutability. The bean lesson is background only; the worked guide teaches collection behavior before the product task.

## Evidence (10 minutes)

Run the source-add and source-remove reference observations, then record the actual capstone selector result and one ownership explanation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day04ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Capstone implementation guidance

Use the assistant-provided architecture note linked in the concept block. Implement and test the stated decision within the existing acceptance and practice allocation; no pre-work design proposal is requested.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
