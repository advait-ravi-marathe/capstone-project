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

Annotated trace of the existing integration test: catalog → eligibility/premium → quote snapshot → repository → selection guard/persistence → direct PostgreSQL readback. Mark the actual failure stop and one prior-taught transaction boundary. No new architecture theory or rejected-abstraction essay is assigned.

## Evidence (10 minutes)

Integration report, short demo, prerequisite gaps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day14BackendSliceIT test`

This fixed acceptance test currently fails at Day 4 `ProductCatalogService.findByCode`; that red result is expected. Do not mark integration passed until the entire component path succeeds.

## Interview-first system-design session (45 minutes inside the existing 85-minute practice block)

**Prompt:** [Q05](../interview/question-bank.md#q05) from the original question bank. Before any hint, reference answer, or AI, write your own initial proposal. Follow the exact 5/5/15/10/10 phases in [the mock rubric](../interview/mock-rubric.md): requirements; capacity and constraints; initial architecture and data model (save before help); bottlenecks, reliability and scaling; trade-offs, failures and follow-ups.

**Schedule:** 2026-11-06. This session replaces 45 minutes of implementation/test practice; the remaining 40 minutes stay for the day’s implementation and tests. Do not add time or mark a partial acceptance gate complete. Use the existing 10-minute evidence block for command, result, assistance, and blocker only. For the exact 40-minute EAI slice and deferred acceptance queue, follow [Sprint 1 session scope](../interview/session-scope.md); the original day stays pending until its full acceptance passes.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 14` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
