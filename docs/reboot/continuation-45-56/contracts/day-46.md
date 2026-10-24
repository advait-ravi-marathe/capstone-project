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

## Practice and evidence

**85-minute practice allocation:**
- 10 minutes: complete the state/date matrix.
- 55 minutes: implement eligibility.
- 20 minutes: run both inclusive/exclusive boundaries and each status at all boundaries.

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
