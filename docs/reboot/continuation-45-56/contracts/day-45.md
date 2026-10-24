# Day 45 — 2026-12-07: Define annual policy term boundaries

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPolicy.termFor`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

For effective date E, return the annual term [E,E+1 year) using LocalDate.plusYears(1), with the renewal window opening exactly30 calendar days before the exclusive end. Preserve the ordinary and February29 cases and include 2023-03-01 → 2024-03-01 → 2024-01-31; plusDays(365) is incorrect.

### Readiness before the task

- Actual Day44 learner readiness; no inference from instructor witness
- Sprint 3 queue S3-D42-2 remains open unless its separate, actual prerequisite-gated evidence has been completed; this Sprint 4 plan does not close it
- Existing value/date guides

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-45.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Interview-first system-design mock (45 minutes; within the 85-minute practice block)

**Prompt:** [Q03 — URL expiry and deterministic time](../../interview/question-bank.md#q03). For this interview question only, save your initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the [100-point rubric](../../interview/mock-rubric.md) with 5/5/15/10/10-minute phases. This is a design-transfer exercise; it adds no product implementation.

**Existing engineering work (40 minutes):** 5 minutes confirm actual Day44 readiness and predict exclusive-end behavior; 25 minutes implement only `PolicyRenewalPreviewPolicy.termFor`; 10 minutes run the named Day45 selector and record its first actual result or blocker.

**Runnable acceptance:** Use the exact learner acceptance command(s) already named above. For Day 45, those remain `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow test`. The command may stop at a prerequisite TODO; record that exact result without substituting an instructor witness.

**Deferred queue:** **S4-D45-1** — The full original date matrix remains required: preserve ordinary and leap-day tuples, include the March 1 anniversary case, and prove the 30-day opening boundary. The 40-minute slice and mock do not complete the original Day45 acceptance.

## Practice and evidence

**Practice allocation (85 minutes):** 45 minutes for the named mock, then 40 minutes for the existing engineering task: 5 minutes to confirm its preconditions and predict the first result, 25 minutes on the bounded learner seam or observation, and 10 minutes for the named selector and actual blocker/result record. This replaces the prior full 85-minute implementation breakdown on this mock day only. Original acceptance remains required; carry any unfinished portion in the named queue without overtime.

**10-minute evidence allocation:** record the saved pre-assistance proposal; actual acceptance command, result or first blocker; changed paths and assistance; and which original acceptance remains open. Do not claim completion from a mock, smoke test, or partial slice.

## Study block — exactly 25 minutes

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-45](<../../assistant-architecture-notes-15-70.md#day-45>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

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
