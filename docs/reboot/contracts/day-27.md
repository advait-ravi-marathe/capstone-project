# Day 27 — 2026-11-15: Fix one evidence-backed regression

**State:** Saved JSON boundary reproducer and fixed learner test are installed; the regression behavior remains a learner acceptance failure until verified.

**Course lesson:** Conditional partial support from [Master Java Unit Testing with Spring Boot & Mockito — in28Minutes Official](https://www.udemy.com/course/learn-unit-testing-with-spring-boot/): “Step 26 - Good Unit Tests” (5:34) and “Step 03 - Using Response Matchers to check status and content” (5:07). Mockito Verify (6:40) is optional later, not stacked. Course access is unconfirmed. Use the [worked regression-hypothesis guide](../teaching/day-27-regression-hypothesis.md#worked-boundary-example); no purchase or extra study time.

## Learner seam

targeted learner test and small production fix. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Select highest-value observed failure; reproduce first, then fix and rerun targeted plus affected contracts. If none exists, add one missing boundary test. Do not invent a defect or add a feature.

## Required edge cases

Before state must show reproducible failure or missing boundary; after targeted and affected checks.

## Prerequisite and isolation

Relevant failed day; saved fixture reproducer. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Use the saved reproducer to trace the regression to its owning module. Use the [worked regression-hypothesis example](../teaching/day-27-regression-hypothesis.md#worked-boundary-example). State what observation would distinguish the competing causes; no architecture template is required.

## Concept (25 minutes)

Boundary evidence, competing regression hypotheses, and the smallest justified change. See the [worked guide](../teaching/day-27-regression-hypothesis.md#worked-boundary-example).

## Evidence (10 minutes)

Before/after assertion and affected-suite result. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

Instructor toy reference: `./mvnw -Dtest=Day27RegressionHypothesisReferenceTest test`.

Learner acceptance: `./mvnw -Plearner-tests -Dtest=Day27ContractTest test`.

This selector loads the saved JSON reproducer and exercises the existing learner policy. The TODO failure is expected and does not represent an observed production defect. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 35 Search Insert Position — boundary search. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Which observation would falsify your preferred cause? What adjacent input distinguishes the competing explanation?

**Observable evidence:** Preserve failing assertion, narrow cause, apply a small repair and rerun affected contracts; if no defect exists, identify a real missing boundary.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A6](../interview-evidence-overlay.md#a6) (anecdotal); [A7](../interview-evidence-overlay.md#a7) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
