# Day 51 — 2026-12-13: Expose a minimal non-binding preview response

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewController.available`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Keep the existing PolicyRenewalPreview DTO and object equality. Actual standalone MVC JSON has exactly eight approved keys and exact values/types, including numeric BigDecimal current premium. No private IDs, action links, acceptance fields or new price.

### Readiness before the task

- Days45–50 ready
- Existing preview DTO; serialization observed before normalizer freeze

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-51.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51PublicPreviewIsMinimalAndContainsNoPrivateOrMutationFields test`
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51SerializedPreviewHasExactlyApprovedFieldsAndNumericPremium test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Interview-first task proposal (5 minutes inside the existing 85-minute practice block)

Before code, hints, worked answers, or AI, save a short prediction: List the exact approved serialized keys, values and JSON types, then list one private and one mutation field that must be absent before changing the response adapter.

Use the remaining 80 minutes only for the unchanged day acceptance. Existing prerequisites still gate the work; do not infer that a prior learner gate passed.

**Relevant patterns:** [http-boundary](../../interview/pattern-library.md#http-boundary), [immutable-snapshot](../../interview/pattern-library.md#immutable-snapshot).

**Run the existing acceptance:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51PublicPreviewIsMinimalAndContainsNoPrivateOrMutationFields test`
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51SerializedPreviewHasExactlyApprovedFieldsAndNumericPremium test`

The proposal is not a pass. Keep unfinished acceptance open and stop at the first prerequisite blocker or at the daily cap. Record the proposal, actual command/result or blocker, changed paths, and assistance in the existing 10-minute evidence block. No retrospective architecture essay.

## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- Required source: **Step 03 - Enhancing the Hello World REST API to return a Bean**, section **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (4:30), access `confirmed_owned`. No viewing is inferred.
- Worked guide: [Inspect the actual JSON projection](../worked/day-51-serialized-public-projection.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Spring: Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/) — Create a Resource Representation Class; Create a Resource Controller.
- [Spring Framework: Return Values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html) — HttpEntity<B>, ResponseEntity<B> table entry.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
