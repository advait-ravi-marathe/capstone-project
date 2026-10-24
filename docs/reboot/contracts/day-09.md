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

**Readiness question (inside the existing evidence slot):** Why does this `SELECT` bind both `journeyId` and `quoteVersionId`? Why is a missing row different from a SQL exception? The `QuoteRepositoryAdapter` accepts those two scope values; authentication and owner authorization remain a separate Day 12 boundary. Point to the reference's resource-closing boundaries; no ORM decision is assigned.

## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Use the existing JDBC adapter, prepared statements, and explicit row-to-snapshot mapping. Bind `journeyId` and `quoteVersionId`, the adapter's composite lookup scope, so input remains data and an absent composite key returns explicit absence. Owner authentication/authorization is separate and is introduced at the Day 12 trusted-principal boundary.](../assistant-architecture-notes.md#day-9-parameterized-snapshot-persistence). No capstone design proposal or post-hoc architecture justification is assigned.

Parameterized JDBC, connection/statement/result resource ownership, mapping a row to a value, and explicit absence. The lesson is background only; the worked guide teaches these raw-JDBC operations before the repository task. No JPA/ORM conversion is assigned.

## Evidence (10 minutes)

Disposable PostgreSQL test output. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only; no architecture proposal or retrospective justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day09ContractTest test`

This selector runs the installed adapter tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Capstone implementation guidance

Use the assistant-provided architecture note linked in the concept block. Implement and test the stated decision within the existing acceptance and practice allocation; no pre-work design proposal is requested.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
