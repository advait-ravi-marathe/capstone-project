# Day 05 — 2026-10-24: Decide HomeSecure eligibility

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/). Section “APPENDIX - Functional Programming - A Quick Review using Q&A Format”: “Core concepts of Functional Programming” (6:43). Section “APPENDIX - Introduction to Functional Programming with Java”: “Step 05 - Do Functional Programming Exercises with Streams, Filters and Lambdas” (5:30). Total 12:13. Supporting material only; use the linked pure-function and decision-table example for the insurance eligibility rule. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-5--pure-eligibility-decision).

## Learner seam

`HomeSecureEligibilityPolicy`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use fixture bounds `100000.00..10000000.00` INR inclusive and required occupancy `OWNER_OCCUPIED`. Return eligible only when occupancy is exact and amount is in range. Reason precedence is missing occupancy/amount → `REQUIRED_FIELD_MISSING`; other occupancy → `OWNER_OCCUPIED_REQUIRED`; out-of-range amount → `REBUILD_AMOUNT_OUT_OF_RANGE`. The decision is training-only, not insurance advice.

## Required edge cases

Amounts `99999.99`, `100000.00`, `10000000.00`, `10000000.01`; absent fields; non-owner-occupied property; simultaneous invalidities use fixture reason precedence.

## Prerequisite and isolation

Day 4; catalog bounds fixture, no live controller. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Record the eligibility invariant and reason-precedence decision table. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Pure functions, invariants and decision tables.

## Evidence (10 minutes)

Decision table mapped to parameterized tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day05ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 383 Ransom Note — frequency counting. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why make eligibility a pure policy? Which exact boundary is eligible?

**Observable evidence:** Turn the frozen decision table into boundary tests; explain why database and HTTP concerns do not belong in the rule.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A7](../interview-evidence-overlay.md#a7) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
