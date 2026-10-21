# Day 05 — 2026-10-24: Decide HomeSecure eligibility

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

**Design/readiness prompt (inside the existing evidence block):** Explain a toy-table row with simultaneous invalidities, then identify the corresponding frozen insurance rule and precedence. Do not assign an unexplained architecture template.

## Concept (25 minutes)

Pure rule evaluation, first-applicable-rule precedence, inclusive boundaries, and deterministic repeated calls. The lesson is context; the worked parcel table teaches the method before the insurance policy task.

## Evidence (10 minutes)

Record the two predicted toy outcomes, then the actual insurance selector result and a short precedence explanation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance. DSA notes remain separate.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day05ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** For a row with multiple invalid fields, which rule applies first and why?

**Observable evidence:** Predict and test the parcel variation, then map the insurance table's existing boundaries and reason precedence without importing toy values.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A7](../interview-evidence-overlay.md#a7) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 5` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
