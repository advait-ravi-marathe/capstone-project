# Day 08 — 2026-10-31: Define PostgreSQL quote schema

**State:** Instructor scaffold installed; learner implementation pending. PostgreSQL acceptance cases are fixed and currently fail because the scaffold migration omits the required constraints.

**Required course background:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section “Getting Started with JPA and Hibernate with Spring and Spring Boot”: “Step 03 - Launching up H2 Console and Creating Course Table in H2” (7:24; item 35019878). “Step 04 - Getting Started with Spring JDBC” (3:58; item 35019874) is optional later. H2 is table-introduction only; use the [worked composite-constraint example](../teaching/day-08-composite-constraints.md) for PostgreSQL keys and SQLSTATEs. Total study: 7:24 + 13:36 + 4:00 readiness = 25 minutes. Catalog verified; playback not verified.

## Learner seam

Versioned `V...__homesecure_quotes.sql` migration. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use `journeys`, `quote_versions`, and `selections`. `quote_versions` primary key is `(journey_id, quote_version_id)`; `selections` references that exact key and has unique `(journey_id, quote_version_id)` plus required `selection_order`. This prevents cross-journey selection at the database boundary and permits distinct versions in one journey.

## Required edge cases

Valid insert; null required field, duplicate pair, orphan quote reference, cross-journey reference rejected; two versions for one journey allowed. Test SQL constraints directly, not services.

## Prerequisite and isolation

Days 1 and 7; SQL seeds bypass services. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Readiness question (inside the existing evidence slot):** Why must the foreign key compare the complete pair? Which invalid state would pass if it checked only `revision_id`? The instructor schema is separate; apply the reasoning to the contract's named tables without expanding scope.

## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Use PostgreSQL constraints for required values, keys, and uniqueness, plus foreign keys for relationships. The database remains the integrity boundary if application validation is bypassed.](../assistant-architecture-notes.md#day-8-database-enforced-quote-invariants). No capstone design proposal or post-hoc architecture justification is assigned.

Composite primary-key identity, matching composite foreign keys, required columns, and observed SQLSTATEs. The H2 lesson does not teach the PostgreSQL constraint matrix; the worked guide does so before the migration task.

## Evidence (10 minutes)

Real PostgreSQL insert/rejection matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only; no architecture proposal or retrospective justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day08ContractTest test`

This selector runs the installed tests against a real embedded PostgreSQL server. It should fail until the learner completes the explicit constraints in `src/main/resources/db/migration/V001__homesecure_quotes.sql`. Such failures are not an infrastructure pass. Infrastructure checks are separate.

## Capstone implementation guidance

Use the assistant-provided architecture note linked in the concept block. Implement and test the stated decision within the existing acceptance and practice allocation; no pre-work design proposal is requested.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
