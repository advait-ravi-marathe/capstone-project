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

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day09ContractTest test`

This selector runs the installed adapter tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
