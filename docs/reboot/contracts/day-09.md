# Day 09 — 2026-11-01: Persist/read immutable quote snapshots

**State:** Instructor scaffold installed; learner implementation pending. Tests use real PostgreSQL and a fixed test-only schema/seed that bypass Days 7–8.

**Required course background:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section “Getting Started with JPA and Hibernate with Spring and Spring Boot”: “Step 07 - Querying Data using Spring JDBC” (5:36; item 35019858). “Step 06 - Inserting and Deleting Data using Spring JDBC” (9:01; item 35019864) is optional later. The course is higher-level context; the [raw-JDBC worked guide](../teaching/day-09-jdbc-boundary.md) teaches the `DataSource`/`PreparedStatement` code before this adapter task. Total: 5:36 + 15:24 + 4:00 readiness = 25 minutes. Catalog verified; playback not verified.

## Learner seam

`QuoteRepositoryAdapter`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Round trip preserves `NUMERIC(14,2)`, UTC timestamps and version ID; unknown `(journeyId, quoteVersionId)` returns explicit absence; reads require supplied journey scope; snapshots are insert-only through this adapter.

## Required edge cases

Round-trip precision, absent ID, wrong journey, update attempt.

## Prerequisite and isolation

Day 8; seeded rows, no quote-creation service. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Readiness question (inside the existing evidence slot):** Why does this `SELECT` bind both scope fields? Why is a missing row different from a SQL exception? Point to the reference's resource-closing boundaries; no ORM decision is assigned.

## Concept (25 minutes)

Parameterized JDBC, connection/statement/result resource ownership, mapping a row to a value, and explicit absence. The lesson is background only; the worked guide teaches these raw-JDBC operations before the repository task. No JPA/ORM conversion is assigned.

## Evidence (10 minutes)

Disposable PostgreSQL test output. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day09ContractTest test`

This selector runs the installed adapter tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 9` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
