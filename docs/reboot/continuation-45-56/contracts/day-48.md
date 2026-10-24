# Day 48 — 2026-12-10: Add owner-scoped PostgreSQL renewal projection

## Contract

**Learner-owned seam:** `JdbcPolicyRenewalReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Use PreparedStatement and both policyId and trusted ownerId predicates. Return exact projection or Optional.empty. Own, foreign, absent, second-owner and SQL-looking literal inputs are covered. Complete ordered policy/event/idempotency snapshots remain identical across every read.

### Readiness before the task

- Main Day9 raw JDBC/Optional and Day36 owner scope
- Existing lifecycle SQL fixture; no new schema

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-48.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalRepositoryAcceptanceTest#day48ReadIsOwnerScopedAndReturnsOnlyRenewalInputsWithoutWrites test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Interview-first task proposal (5 minutes inside the existing 85-minute practice block)

Before code, hints, worked answers, or AI, save a short prediction: Write the two SQL predicates, expected owned/foreign/absent results, and the full tables that must remain unchanged before editing the JDBC read.

Use the remaining 80 minutes only for the unchanged day acceptance. Existing prerequisites still gate the work; do not infer that a prior learner gate passed.

**Relevant patterns:** [ownership-boundary](../../interview/pattern-library.md#ownership-boundary), [transaction-boundary](../../interview/pattern-library.md#transaction-boundary).

**Run the existing acceptance:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalRepositoryAcceptanceTest#day48ReadIsOwnerScopedAndReturnsOnlyRenewalInputsWithoutWrites test`

The proposal is not a pass. Keep unfinished acceptance open and stop at the first prerequisite blocker or at the daily cap. Record the proposal, actual command/result or blocker, changed paths, and assistance in the existing 10-minute evidence block. No retrospective architecture essay.

## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- Required source: **Step 07 - Querying Data using Spring JDBC**, section **Getting Started with JPA and Hibernate with Spring and Spring Boot** (5:36), access `confirmed_owned`. No viewing is inferred.
- Worked guide: [Bind both keys and compare complete rows](../worked/day-48-scoped-jdbc-observation.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Java SE 21: PreparedStatement](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html) — setString(int, String); executeQuery().
- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.1) — 9.2.1 Safe Methods; 9.3.1 GET.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required. Optional lesson references are deferred recovery only and do not add time.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
