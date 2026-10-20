# Day 22 — 2026-11-10: Add safe request correlation

**State:** Fixed filter scaffold and learner acceptance tests are installed; the filter TODO remains unimplemented.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/). Section “APPENDIX - Build Java Web Application with Spring, Spring Boot and Hibernate”: “Step 09 - Quick Overview - Importance of Logging with Spring Boot” (9:53). Section “Creating a Java REST API with Spring Boot, Spring Framework and Hibernate”: “Step 25 - Monitoring APIs with Spring Boot Actuator” (6:54). Total 16:47. These introduce logging and monitoring; use the linked request-ID/privacy trace. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-22--correlation-and-privacy).

## Learner seam

request filter / error mapping. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Every response has `X-Request-Id` matching `[A-Za-z0-9._-]{1,64}`. Missing IDs are generated; malformed supplied IDs replaced. One error log references the selected ID, never applicant body/token/cookie. Request IDs are not metric labels.

## Required edge cases

Absent, valid, malformed ID; captured-log redaction assertions.

## Prerequisite and isolation

Day 3; fixed request fixtures and fake logger. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show request-ID creation/validation and which log fields must be excluded. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Observability vs privacy, logs vs metrics.

## Evidence (10 minutes)

Captured log assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day22ContractTest test`

The fixed request-filter tests run through the named selector and currently fail at the explicit correlation TODO. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 121 (D+21 from Day 1), LC 11 (D+7 from Day 15), LC 209 (D+3 from Day 19). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** A user reports an error. How can you follow the request without logging its body or token?

**Observable evidence:** Find matching correlation ID; show unsafe-ID replacement and log-redaction assertions; explain why IDs are poor metric labels.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A1](../interview-evidence-overlay.md#a1) (anecdotal); [A7](../interview-evidence-overlay.md#a7) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
