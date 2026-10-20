# Day 06 — 2026-10-25: Calculate synthetic premium

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`PremiumCalculator`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use fixture rate `0.00125`, `BUILDING` adjustment `0.00`, and `BUILDING_CONTENTS` adjustment `125.00` INR. Calculate `rebuildAmountRupees * rate + selectedCoverage.annualAdjustmentRupees`, then round to scale 2 using `HALF_UP`; currency is INR and taxes are excluded. Reject negative/null amount and unknown coverage; zero amount is allowed at this pure-calculator seam although Day 5 eligibility rejects it. No floating-point money.

## Required edge cases

Zero and negative; `100.00 * 0.00125 = 0.125` rounds to `0.13`; contents adjustment; deterministic same-input repeat.

## Prerequisite and isolation

Day 4; fixed eligible input and rate fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Value types, BigDecimal precision/rounding [C].

## Evidence (10 minutes)

Hand-calculated expected fixture, independent of implementation. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day06ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 169 Majority Element. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
