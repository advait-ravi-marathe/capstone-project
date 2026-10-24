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

## Interview-first task proposal (5 minutes inside the existing 85-minute practice block)

Before code, hints, worked answers, or AI, save a short prediction: Map missing, foreign and each ineligible outcome to its exact status/code before editing the controller; predict why missing and foreign must be indistinguishable.

Use the remaining 80 minutes only for the unchanged day acceptance. Existing prerequisites still gate the work; do not infer that a prior learner gate passed.

**Relevant patterns:** [http-boundary](../../interview/pattern-library.md#http-boundary), [ownership-boundary](../../interview/pattern-library.md#ownership-boundary).

**Run the existing acceptance:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes+day50FoundIneligibleGetMaps409WithoutCallingAvailablePreviewBuilder test`

The proposal is not a pass. Keep unfinished acceptance open and stop at the first prerequisite blocker or at the daily cap. Record the proposal, actual command/result or blocker, changed paths, and assistance in the existing 10-minute evidence block. No retrospective architecture essay.

## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- Required source: **Step 11 - Implementing Exception Handling - 404 Resource Not Found**, section **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (7:49), access `confirmed_owned`. No viewing is inferred.
- Worked guide: [Map known unavailable outcomes safely](../worked/day-50-unavailable-response-table.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Spring Framework: Return Values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html) — HttpEntity<B>, ResponseEntity<B> table entry.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required. Optional lesson references are deferred recovery only and do not add time.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
