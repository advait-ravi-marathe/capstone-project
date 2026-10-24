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

**Design/readiness prompt (inside the existing evidence block):** Explain why rounding before addition changes the worked result, and why zero is allowed by this calculator contract but rejected by Day 5 eligibility. Do not add pricing rules.

## Concept (25 minutes)

Decimal-string construction, exact arithmetic, `HALF_UP` at the declared final boundary, and scale-sensitive representation. The course is conditional; the worked supplement teaches the demonstrated arithmetic before the frozen premium task.

## Evidence (10 minutes)

Record the exact and premature-rounding outputs, then the actual learner selector result and the frozen premium calculation outcome. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, and assistance. DSA notes remain separate.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day06ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 6` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
