# Day 54 — 2026-12-16: Verify the owner-scoped preview against PostgreSQL

## Contract

**Learner-owned seam:** `GET /api/policies/{policyId}/renewal-preview`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Use the actual JDBC store, embedded PostgreSQL and standalone MockMvc. Own is200; foreign and absent are indistinguishable404; anonymous is401. Compare exact public response and complete ordered snapshots of all three existing tables after each request.

### Readiness before the task

- Actual Days45–51 semantic gates
- Disposable PostgreSQL instructor fixture

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-54.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalPostgresAcceptanceTest#day54OwnerScopedEndpointReadsPostgresAndLeavesPolicyRowsUnchanged test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Interview-first task proposal (5 minutes inside the existing 85-minute practice block)

Before code, hints, worked answers, or AI, save a short prediction: List owner/foreign/absent/anonymous response expectations and the three ordered database snapshots to compare after every request before running the PostgreSQL acceptance.

Use the remaining 80 minutes only for the unchanged day acceptance. Existing prerequisites still gate the work; do not infer that a prior learner gate passed.

**Relevant patterns:** [ownership-boundary](../../interview/pattern-library.md#ownership-boundary), [transaction-boundary](../../interview/pattern-library.md#transaction-boundary).

**Run the existing acceptance:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalPostgresAcceptanceTest#day54OwnerScopedEndpointReadsPostgresAndLeavesPolicyRowsUnchanged test`

The proposal is not a pass. Keep unfinished acceptance open and stop at the first prerequisite blocker or at the daily cap. Record the proposal, actual command/result or blocker, changed paths, and assistance in the existing 10-minute evidence block. No retrospective architecture essay.

## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- Required source: **Step 15 - Writing an Integration Test using @SpringBootTest**, section **Section 2 - Unit Testing With Spring Boot And Mockito** (8:51), access `unconfirmed_reference_only`. No viewing is inferred.
- Worked guide: [Compare full database state around the read](../worked/day-54-complete-database-observation.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Spring Boot: Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) — MOCK versus RANDOM_PORT behavior in opening section.
- [PostgreSQL 18: SET TRANSACTION](https://www.postgresql.org/docs/18/sql-set-transaction.html) — Description: transaction access mode READ ONLY.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
