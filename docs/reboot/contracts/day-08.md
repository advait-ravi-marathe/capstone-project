# Day 08 — 2026-10-27: Define PostgreSQL quote schema

**State:** Instructor scaffold installed; learner implementation pending. PostgreSQL acceptance cases are fixed and currently fail because the scaffold migration omits the required constraints.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/). Section “Getting Started with JPA and Hibernate with Spring and Spring Boot”: “Step 03 - Launching up H2 Console and Creating Course Table in H2” (7:24); “Step 04 - Getting Started with Spring JDBC” (3:58). Total 11:22. H2/JDBC is an introduction only; keep PostgreSQL and use the linked schema/constraint example. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-8---postgresql-schema-and-constraints).

## Learner seam

Versioned `V...__homesecure_quotes.sql` migration. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use `journeys`, `quote_versions`, and `selections`. `quote_versions` primary key is `(journey_id, quote_version_id)`; `selections` references that exact key and has unique `(journey_id, quote_version_id)` plus required `selection_order`. This prevents cross-journey selection at the database boundary and permits distinct versions in one journey.

## Required edge cases

Valid insert; null required field, duplicate pair, orphan quote reference, cross-journey reference rejected; two versions for one journey allowed. Test SQL constraints directly, not services.

## Prerequisite and isolation

Days 1 and 7; SQL seeds bypass services. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**PostgreSQL design prompt:** Identify primary/composite keys, normalization, constraints, and transaction/isolation boundary relevant to this day's schema/write; do not add infrastructure beyond the contract. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Relational keys, normalization, integrity [E].

## Evidence (10 minutes)

Real PostgreSQL insert/rejection matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day08ContractTest test`

This selector runs the installed tests against a real embedded PostgreSQL server. It should fail until the learner completes the explicit constraints in `src/main/resources/db/migration/V001__homesecure_quotes.sql`. Such failures are not an infrastructure pass. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 121 (D+7 from Day 1), LC 383 (D+3 from Day 5). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Which invalid state is impossible even if Java is bypassed? Why does pair uniqueness not itself prove same-journey ownership?

**Observable evidence:** Draw keys and references; explain cross-journey protection; execute valid and rejected direct SQL writes.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
