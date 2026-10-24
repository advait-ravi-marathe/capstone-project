# Day 03 — 2026-10-26: Map failures consistently

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Required course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section “Creating a Java REST API with Spring Boot, Spring Framework and Hibernate”: “Step 12 - Implementing Generic Exception Handling for all Resources” (11:29; item 35018806). Study the [Day 3 safe-errors guide](../teaching/day-03-safe-errors.md) before implementation. “Step 11 - Implementing Exception Handling - 404 Resource Not Found” (7:49; item 35018810) is optional later reference, not stacked into today. Catalog metadata verified 2026-10-21; playback not verified. See the [Days 1–30 map](../course-learning-map.md).

## Learner seam

`ApiExceptionHandler`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Preserve the Day 2 error shape `{"code":"...","message":"..."}`. Unknown resource is HTTP 404 with `RESOURCE_NOT_FOUND` and `Resource was not found`; version conflict is HTTP 409 with `VERSION_CONFLICT` and `The resource version conflicts`; unexpected failure is HTTP 500 with `INTERNAL_ERROR` and `The request could not be completed`. Never return stack, SQL, request body or internal exception text.

## Required edge cases

Fixture not-found, conflict and unexpected exception; verify safe fields and no secret text.

## Prerequisite and isolation

Day 2; mocked service throws fixed fixture exceptions. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked design question (after the guide):** Why does a client receive a stable error code while internal exception text stays out of the response? Do not create an ADR or architecture case study.

## Concept (25 minutes)

Study the required generic exception-handling lesson and the Day 3 guide: controller failure, matching exception handler, `ResponseEntity` status/body, and a fixed safe response for unknown errors. Do not copy the toy strings into the capstone or require the later Day 12 authorization distinction.

## Evidence (10 minutes)

Three negative response snapshots. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Instructor reference and preserved learner command

Run the independent worked example first: `./mvnw -Dtest=Day03SafeErrorsReferenceTest test`. Then use the existing learner selector: `./mvnw -Plearner-tests -Dtest=Day03ContractTest test`. This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Interview-first system-design session (45 minutes inside the existing 85-minute practice block)

**Prompt:** [Q08](../interview/question-bank.md#q08) from the original question bank. Before any hint, reference answer, or AI, write your own initial proposal. Follow the exact 5/5/15/10/10 phases in [the mock rubric](../interview/mock-rubric.md): requirements; capacity and constraints; initial architecture and data model (save before help); bottlenecks, reliability and scaling; trade-offs, failures and follow-ups.

**Schedule:** 2026-10-26. This session replaces 45 minutes of implementation/test practice; the remaining 40 minutes stay for the day’s implementation and tests. Do not add time or mark a partial acceptance gate complete. Use the existing 10-minute evidence block for command, result, assistance, and blocker only. For the exact 40-minute EAI slice and deferred acceptance queue, follow [Sprint 1 session scope](../interview/session-scope.md); the original day stays pending until its full acceptance passes.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided first encounter:** [LeetCode 383 Ransom Note](https://leetcode.com/problems/ransom-note/). Use the Premium editorial/solution only after stating the problem in your own words and making a first attempt; the resulting work remains guided/assisted and earns no independent-transfer credit. Preserve the original canonical anchor identities and record any actual attempt only when it occurs.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 3` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
