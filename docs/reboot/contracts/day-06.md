# Day 06 — 2026-10-25: Calculate synthetic premium

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Course lesson:** Conditional reference to [Java Programming for Complete Beginners — in28Minutes Official](https://www.udemy.com/course/java-programming-tutorial-for-beginners/); “Primitive Data Types And Alternatives in Java Programming”: “Step 05 - Introduction to BigDecimal Java Class” (6:58), “Step 06 - BigDecimal Puzzles - Adding Integers” (3:13), and “Step 07 - BigDecimal Exercises - Simple Interest Calculation” (11:34), 21:45 total. Direct BigDecimal foundation; use the contract’s premium formula and the linked rounding guide. Course access is unconfirmed; no purchase/enrollment. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-6---decimal-calculation-and-rounding).

## Learner seam

`PremiumCalculator`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use fixture rate `0.00125`, `BUILDING` adjustment `0.00`, and `BUILDING_CONTENTS` adjustment `125.00` INR. Calculate `rebuildAmountRupees * rate + selectedCoverage.annualAdjustmentRupees`, then round to scale 2 using `HALF_UP`; currency is INR and taxes are excluded. Reject negative/null amount and unknown coverage; zero amount is allowed at this pure-calculator seam although Day 5 eligibility rejects it. No floating-point money.

## Required edge cases

Zero and negative; `100.00 * 0.00125 = 0.125` rounds to `0.13`; contents adjustment; deterministic same-input repeat.

## Prerequisite and isolation

Day 4; fixed eligible input and rate fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show which input fields affect premium and where decimal rounding occurs. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Value types, BigDecimal precision/rounding [C].

## Evidence (10 minutes)

Hand-calculated expected fixture, independent of implementation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day06ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 1 (D+3 from Day 3). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why can binary floating point be wrong here? Where are scale and rounding defined?

**Observable evidence:** Hand-calculate a rounding boundary; explain value equality versus representation without inventing a new formula.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official). These references guide practice; they do not establish question frequency or predict interview content.
