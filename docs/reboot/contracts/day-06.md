# Day 06 — 2026-10-25: Calculate synthetic premium

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

**Design/readiness prompt (inside the existing evidence block):** Explain why rounding before addition changes the worked result, and why zero is allowed by this calculator contract but rejected by Day 5 eligibility. Do not add pricing rules.

## Concept (25 minutes)

Decimal-string construction, exact arithmetic, `HALF_UP` at the declared final boundary, and scale-sensitive representation. The course is conditional; the worked supplement teaches the demonstrated arithmetic before the frozen premium task.

## Evidence (10 minutes)

Record the exact and premature-rounding outputs, then the actual learner selector result and the frozen premium calculation outcome. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance. DSA notes remain separate.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day06ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 1 (D+3 from Day 3). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** What changes if rounding occurs before addition? Why does zero have different meaning at the calculator and eligibility seams?

**Observable evidence:** Reproduce both exact and early-rounded examples, then identify the existing premium formula's named rounding boundary without inventing a formula.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official). These references guide practice; they do not establish question frequency or predict interview content.
