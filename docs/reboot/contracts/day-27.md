# Day 27 — 2026-11-19: Fix one evidence-backed regression

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

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-27](<../assistant-architecture-notes-15-70.md#day-27>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Boundary evidence, competing regression hypotheses, and the smallest justified change. See the [worked guide](../teaching/day-27-regression-hypothesis.md#worked-boundary-example).

## Evidence (10 minutes)

Before/after assertion and affected-suite result. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

Instructor toy reference: `./mvnw -Dtest=Day27RegressionHypothesisReferenceTest test`.

Learner acceptance: `./mvnw -Plearner-tests -Dtest=Day27ContractTest test`.

This selector loads the saved JSON reproducer and exercises the existing learner policy. The TODO failure is expected and does not represent an observed production defect. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
