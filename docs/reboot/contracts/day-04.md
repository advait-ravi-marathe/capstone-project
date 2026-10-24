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

**Design/readiness prompt (inside the existing evidence block):** After the worked example, explain who owns the source list, why a record alone does not prevent caller mutation, and one copying cost or mutable-element limitation. Do not add an architecture essay.

## Concept (25 minutes)

Record component references, collection aliasing, `List.copyOf`, and limits of shallow immutability. The bean lesson is background only; the worked guide teaches collection behavior before the product task.

## Evidence (10 minutes)

Run the source-add and source-remove reference observations, then record the actual capstone selector result and one ownership explanation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance. DSA notes remain separate.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day04ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 4` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
