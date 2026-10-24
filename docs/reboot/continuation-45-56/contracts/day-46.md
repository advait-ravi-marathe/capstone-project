# Day 46 — 2026-12-08: Enforce renewal window and lifecycle eligibility

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPolicy.eligibility`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The opening date is included and the next term boundary is excluded. CANCEL_SCHEDULED and CANCELLED both return NOT_RENEWABLE before time-window evaluation, including before, at, and after both boundaries.

### Readiness before the task

- Day45 term values

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-46.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day46RenewalWindowHasFixedOpenCloseAndLifecycleBoundaries test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Interview-first task proposal (5 minutes inside the existing 85-minute practice block)

Before code, hints, worked answers, or AI, save a short prediction: Write the open/closed date-boundary outcomes for each lifecycle status before editing eligibility; identify the exact existing test rows that distinguish inclusive from exclusive boundaries.

Use the remaining 80 minutes only for the unchanged day acceptance. Existing prerequisites still gate the work; do not infer that a prior learner gate passed.

**Relevant patterns:** [deterministic-time](../../interview/pattern-library.md#deterministic-time), [frontend-state-machine](../../interview/pattern-library.md#frontend-state-machine).

**Run the existing acceptance:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day46RenewalWindowHasFixedOpenCloseAndLifecycleBoundaries test`

The proposal is not a pass. Keep unfinished acceptance open and stop at the first prerequisite blocker or at the daily cap. Record the proposal, actual command/result or blocker, changed paths, and assistance in the existing 10-minute evidence block. No retrospective architecture essay.

## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- No course video is required for this day; use the supplied focused official references and project-authored worked example.
- Worked guide: [A state-and-date decision table](../worked/day-46-window-decision-table.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Java SE 21: Clock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) — fixed(Instant, ZoneId).
- [Java SE 21: LocalDate](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html) — plusYears(long); minusDays(long); isBefore(ChronoLocalDate).

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required. Optional lesson references are deferred recovery only and do not add time.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
