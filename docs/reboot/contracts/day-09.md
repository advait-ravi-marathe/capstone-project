# Day 09 — 2026-10-28: Persist/read immutable quote snapshots

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

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 238 Product of Array Except Self — prefix/suffix products. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** What does empty `Optional` mean here, and how is it different from a database failure?

**Observable evidence:** Trace bound parameters and resource closure in the teaching case, then explain the existing scoped read, decimal/time contract, and explicit absence.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
