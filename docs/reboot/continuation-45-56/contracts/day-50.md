# Day 50 — 2026-12-12: Map unavailable previews to stable HTTP errors

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewController.unavailable`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Missing and foreign rows return identical safe404. TOO_EARLY, TERM_ENDED, NOT_RENEWABLE map to the fixed409 code/message. None invoke available/buildPreview. Do not add infrastructure503/500 behavior to this seam.

### Readiness before the task

- Days45–49 actual policy and identity seams
- Day51 stays TODO in chronology proof

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-50.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes+day50FoundIneligibleGetMaps409WithoutCallingAvailablePreviewBuilder test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.
## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record the actual command/output, source SHA, result or first blocker, changed paths, and assistance. Do not infer learner completion from a green instructor reference or write a post-hoc architecture justification.

## Study block — exactly 25 minutes

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-50](<../../assistant-architecture-notes-15-70.md#day-50>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

- Required source: **Step 11 - Implementing Exception Handling - 404 Resource Not Found**, section **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (7:49), access `confirmed_owned`. No viewing is inferred.
- Worked guide: [Map known unavailable outcomes safely](../worked/day-50-unavailable-response-table.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Spring Framework: Return Values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html) — HttpEntity<B>, ResponseEntity<B> table entry.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required. Optional lesson references are deferred recovery only and do not add time.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
