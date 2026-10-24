# Day 45 — 2026-12-07: Define annual policy term boundaries

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPolicy.termFor`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

For effective date E, return the annual term [E,E+1 year) using LocalDate.plusYears(1), with the renewal window opening exactly30 calendar days before the exclusive end. Preserve the ordinary and February29 cases and include 2023-03-01 → 2024-03-01 → 2024-01-31; plusDays(365) is incorrect.

### Readiness before the task

- Actual Day44 learner readiness; no inference from instructor witness
- Existing value/date guides

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-45.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Practice and evidence

**85-minute practice allocation:**
- 10 minutes: record goal, question, assumption and three date cases.
- 55 minutes: implement termFor.
- 20 minutes: run original, leap and crossing-leap tests.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- Required source: **Step 11 - Java Dates - Introduction to LocalDate, LocalTime and LocalDateTime**, section **Reference Types in Java Programming** (4:15), access `unconfirmed_reference_only`. No viewing is inferred.
- Worked guide: [Discovery example, then an annual date](../worked/day-45-discovery-anniversary.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Java SE 21: LocalDate](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html) — plusYears(long); minusDays(long); isBefore(ChronoLocalDate).
- [GOV.UK: Plan user research for your service](https://www.gov.uk/service-manual/user-research/plan-user-research-for-your-service) — Agree research questions.
- [GOV.UK: Writing user stories](https://www.gov.uk/service-manual/agile-delivery/writing-user-stories) — Acceptance criteria.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
