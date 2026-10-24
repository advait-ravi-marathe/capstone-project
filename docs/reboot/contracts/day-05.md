# Day 05 — 2026-10-28: Decide HomeSecure eligibility

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Required course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section “APPENDIX - Functional Programming - A Quick Review using Q&A Format”: “Core concepts of Functional Programming” (6:43; item 49578631). “Step 05 - Do Functional Programming Exercises with Streams, Filters and Lambdas” (5:30; item 35017372) is optional later; streams are not required. Catalog titles verified; playback not verified. First work the [decision-table guide](../teaching/day-05-decision-table.md), then transfer the method to the frozen insurance contract. Total study remains 25 minutes: 6:43 lesson, 14:17 worked guide, 4:00 readiness.

## Learner seam

`HomeSecureEligibilityPolicy`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use fixture bounds `100000.00..10000000.00` INR inclusive and required occupancy `OWNER_OCCUPIED`. Return eligible only when occupancy is exact and amount is in range. Reason precedence is missing occupancy/amount → `REQUIRED_FIELD_MISSING`; other occupancy → `OWNER_OCCUPIED_REQUIRED`; out-of-range amount → `REBUILD_AMOUNT_OUT_OF_RANGE`. The decision is training-only, not insurance advice.

## Required edge cases

Amounts `99999.99`, `100000.00`, `10000000.00`, `10000000.01`; absent fields; non-owner-occupied property; simultaneous invalidities use fixture reason precedence.

## Prerequisite and isolation

Day 4; catalog bounds fixture, no live controller. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.



## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Keep eligibility a deterministic policy over supplied product and applicant snapshots. Do not add persistence or controller dependencies; this keeps access and decision responsibilities separate.](../assistant-architecture-notes.md#day-5-pure-eligibility-policy). No capstone design proposal or post-hoc architecture justification is assigned.

Pure rule evaluation, first-applicable-rule precedence, inclusive boundaries, and deterministic repeated calls. The lesson is context; the worked parcel table teaches the method before the insurance policy task.

## Evidence (10 minutes)

Record the two predicted toy outcomes, then the actual insurance selector result and a short precedence explanation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day05ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Capstone implementation guidance

Use the assistant-provided architecture note linked in the concept block. Implement and test the stated decision within the existing acceptance and practice allocation; no pre-work design proposal is requested.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
