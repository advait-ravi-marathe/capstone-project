# Day 14 — 2026-11-02: Milestone 1 backend vertical slice

**State:** Real PostgreSQL backend-slice acceptance test installed and tagged `learnerAcceptance`. It wires the actual fixture, catalog, eligibility, premium, quote, repository, selection, and read-query components. It currently fails at the first explicit learner TODO; a green readiness test is not a substitute for this path. The test reads persisted quotes/selections directly from PostgreSQL and does not call Day 15's summary query.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/). Review only unfamiliar material. Section “Creating a Java REST API with Spring Boot, Spring Framework and Hibernate”: “Step 04 - What's happening in the background? Spring Boot Starters & Autoconfig” (9:06). Section “Getting Started with Spring Boot”: “Step 13 - Understanding Spring Boot vs Spring vs Spring MVC” (3:55). Total 13:01. Use the linked request/data-ownership trace. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-14--requestdata-ownership-trace).

## Learner seam

`BackendSliceIT`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Real product → eligible quote → persist → owner selection → direct PostgreSQL readback. Duplicate semantics remain Day 1’s; stale, expired, invalid and unauthorized cases cannot write. Rejections are checked for zero quote/selection writes using real PostgreSQL and completed production policies.

## Required edge cases

Full success readback; duplicate; stale/expired; invalid; unauthorized with no side effect.

## Prerequisite and isolation

Days 1–13 are required; the integration gate contains no business-policy fakes. The integration test uses no business-rule fakes; it runs against disposable PostgreSQL and exposes the earliest incomplete component.

**System-design prompt (within the 25-minute concept block):** Draw the full product-to-database path and list missing prerequisites; keep the gate blocked until real components pass. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Trace request lifecycle, data ownership, transaction boundary.

## Evidence (10 minutes)

Integration report, short demo, prerequisite gaps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day14BackendSliceIT test`

This fixed acceptance test currently fails at Day 4 `ProductCatalogService.findByCode`; that red result is expected. Do not mark integration passed until the entire component path succeeds.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 49 (D+7 from Day 7), LC 125 (D+3 from Day 11). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Trace quote creation and selection end to end. Defend one transaction boundary and one rejected abstraction.

**Observable evidence:** 20-minute mock replaces implementation: 5 minutes flow, 5 failure demo, 5 alternative/cost, 5 ownership reflection. Use actual gate output; do not claim a demo with fake components is integration.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

**Milestone timebox:** Use 20 minutes from the existing 85-minute implementation block (65 build/test + 20 mock); do not add hours. If the integration gate is blocked, explain the actual failing boundary and next diagnostic step, without presenting a passing demo.
