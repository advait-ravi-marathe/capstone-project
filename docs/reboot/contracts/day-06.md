# Day 06 — 2026-10-29: Calculate synthetic premium

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Conditional course reference:** [Java Programming for Complete Beginners — in28Minutes Official](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) (course 1535678; access unconfirmed; no purchase/enrollment), section “Primitive Data Types And Alternatives in Java Programming”: “Step 05 - Introduction to BigDecimal Java Class” (6:58; item 9565516). Other BigDecimal videos are optional, not stacked today. Use the [worked rounding guide](../teaching/day-06-decimal-rounding.md) before implementation. If course access is unavailable, the official Java 21 BigDecimal reference takes seven minutes. Total study remains 25 minutes: 6:58 or 7:00 reference, 14:02 or 14:00 worked guide, 4:00 readiness.

## Learner seam

`PremiumCalculator`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use fixture rate `0.00125`, `BUILDING` adjustment `0.00`, and `BUILDING_CONTENTS` adjustment `125.00` INR. Calculate `rebuildAmountRupees * rate + selectedCoverage.annualAdjustmentRupees`, then round to scale 2 using `HALF_UP`; currency is INR and taxes are excluded. Reject negative/null amount and unknown coverage; zero amount is allowed at this pure-calculator seam although Day 5 eligibility rejects it. No floating-point money.

## Required edge cases

Zero and negative; `100.00 * 0.00125 = 0.125` rounds to `0.13`; contents adjustment; deterministic same-input repeat.

## Prerequisite and isolation

Day 4; fixed eligible input and rate fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.



## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Use `BigDecimal` with the contract’s explicit scale and rounding rule. Avoid binary floating-point so the same input produces a stable monetary result.](../assistant-architecture-notes.md#day-6-explicit-decimal-calculation). No capstone design proposal or post-hoc architecture justification is assigned.

Decimal-string construction, exact arithmetic, `HALF_UP` at the declared final boundary, and scale-sensitive representation. The course is conditional; the worked supplement teaches the demonstrated arithmetic before the frozen premium task.

## Evidence (10 minutes)

Record the exact and premature-rounding outputs, then the actual learner selector result and the frozen premium calculation outcome. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day06ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Capstone implementation guidance

Use the assistant-provided architecture note linked in the concept block. Implement and test the stated decision within the existing acceptance and practice allocation; no pre-work design proposal is requested.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
