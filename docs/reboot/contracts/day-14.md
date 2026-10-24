# Day 14 — 2026-11-06: Milestone 1 backend vertical slice

**State:** Real PostgreSQL backend-slice acceptance test installed and tagged `learnerAcceptance`. It wires the actual fixture, catalog, eligibility, premium, quote, repository, selection, and read-query components. It currently fails at the first explicit learner TODO; a green readiness test is not a substitute for this path. The test reads persisted quotes/selections directly from PostgreSQL and does not call Day 15's summary query.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/). Review only unfamiliar material. Section “Creating a Java REST API with Spring Boot, Spring Framework and Hibernate”: “Step 04 - What's happening in the background? Spring Boot Starters & Autoconfig” (9:06). Section “Getting Started with Spring Boot”: “Step 13 - Understanding Spring Boot vs Spring vs Spring MVC” (3:55). Total 13:01. Use the linked request/data-ownership trace. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-14--trace-and-diagnose-the-real-backend-slice).

## Learner seam

`BackendSliceIT`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Real product → eligible quote → persist → owner selection → direct PostgreSQL readback. Duplicate semantics remain Day 1’s; stale, expired, invalid and unauthorized cases cannot write. Rejections are checked for zero quote/selection writes using real PostgreSQL and completed production policies.

## Required edge cases

Full success readback; duplicate; stale/expired; invalid; unauthorized with no side effect.

## Prerequisite and isolation

Days 1–13 are required; the integration gate contains no business-policy fakes. The integration test uses no business-rule fakes; it runs against disposable PostgreSQL and exposes the earliest incomplete component.

**Readiness question (inside the existing evidence slot):** Trace one observed positive or blocked path and identify its earliest failing boundary. Name a previously taught value-ownership or transaction guarantee; missing prerequisite evidence remains a blocker.

## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Use the bounded integration test to identify and report the first incomplete dependency. Preserve existing composition and learner TODOs; do not redesign wiring or replace a missing behavior with a fake pass.](../assistant-architecture-notes.md#day-14-diagnose-the-existing-vertical-slice). No capstone design proposal or post-hoc architecture justification is assigned.

Annotated trace of the existing integration test: catalog → eligibility/premium → quote snapshot → repository → selection guard/persistence → direct PostgreSQL readback. Mark the actual failure stop and one prior-taught transaction boundary. No new architecture theory or rejected-abstraction essay is assigned.

## Evidence (10 minutes)

Integration report, short demo, prerequisite gaps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only; no architecture proposal or retrospective justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day14BackendSliceIT test`

This fixed acceptance test currently fails at Day 4 `ProductCatalogService.findByCode`; that red result is expected. Do not mark integration passed until the entire component path succeeds.

## Interview-first system-design session (45 minutes inside the existing 85-minute practice block)

**Prompt:** [Q05](../interview/question-bank.md#q05) from the original question bank. Before any hint, reference answer, or AI, write your own initial proposal. Follow the exact 5/5/15/10/10 phases in [the mock rubric](../interview/mock-rubric.md): requirements; capacity and constraints; initial architecture and data model (save before help); bottlenecks, reliability and scaling; trade-offs, failures and follow-ups.

**Schedule:** 2026-11-06. This session replaces 45 minutes of implementation/test practice; the remaining 40 minutes stay for the day’s implementation and tests. Do not add time or mark a partial acceptance gate complete. Use the existing 10-minute evidence block for command, result, assistance, and blocker only. For the exact 40-minute EAI slice and deferred acceptance queue, follow [Sprint 1 session scope](../interview/session-scope.md); the original day stays pending until its full acceptance passes.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
