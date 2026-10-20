# Day 03 — 2026-10-22: Map failures consistently

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/); section “Creating a Java REST API with Spring Boot, Spring Framework and Hibernate”. Watch: “Step 11 - Implementing Exception Handling - 404 Resource Not Found” (7:49); “Step 12 - Implementing Generic Exception Handling for all Resources” (11:29). 19:18 viewing. The course example is a social-media API; adapt only its failure boundary to the synthetic quote case. See the [Days 1–30 map](../course-learning-map.md) for course verification and the total-time rule.

## Learner seam

`ApiExceptionHandler`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Preserve the Day 2 error shape `{"code":"...","message":"..."}`. Unknown resource is HTTP 404 with `RESOURCE_NOT_FOUND` and `Resource was not found`; version conflict is HTTP 409 with `VERSION_CONFLICT` and `The resource version conflicts`; unexpected failure is HTTP 500 with `INTERNAL_ERROR` and `The request could not be completed`. Never return stack, SQL, request body or internal exception text.

## Required edge cases

Fixture not-found, conflict and unexpected exception; verify safe fields and no secret text.

## Prerequisite and isolation

Day 2; mocked service throws fixed fixture exceptions. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Map one expected domain failure and one unexpected failure across the HTTP boundary. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

HTTP status semantics and failure boundaries [B].

## Evidence (10 minutes)

Three negative response snapshots. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day03ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 1 Two Sum — hash lookup. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** When are 400, 404, 409 and 500 appropriate? Which details must never reach a client?

**Observable evidence:** Predict mapped responses; explain sanitized 500 and show one test per error class. Distinguish 401 from 403 without changing the contract’s non-disclosing 404 policy.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
