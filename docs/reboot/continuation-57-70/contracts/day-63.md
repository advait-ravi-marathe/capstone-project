# Day 63 — 2026-12-25: Run the security trace and recovery checkpoint

## Contract

**Learner-owned seam:** `Readiness evidence and request trace`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Review the authorization path end to end, record the prior evidence gate, and take the timed mock checkpoint. If security acceptance is still red, repair it inside the same engineering budget before any planned next task.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.

**Acceptance test:** `src/test/java/assignments/local/LocalReadinessGateAcceptanceTest.java`
**Acceptance selector:** `day63SecurityCheckpointKeepsRecoveryInsideDailyBudget`
**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day63SecurityCheckpointKeepsRecoveryInsideDailyBudget test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner-first task proposal (5 minutes within the 85-minute practice block)

Before the recovery checkpoint, name the actual Day 62 security evidence flag, predict missing/false/null/true outcomes, and state the first failed prerequisite. Validate the remaining practice minutes are within 0..85.

Write the prediction before opening references, worked examples, hints, or AI. Spend the remaining 80 minutes on the existing acceptance only; this proposal is not completion evidence. Keep any unmet acceptance open as **S5-D63-RECOVERY**.

## Selector limits and open evidence

- The selector covers only day-62-security=false with 85 minutes. Missing/null/true evidence, other values in 0..85, out-of-range rejection, and both no-new-work branches remain unproven by this method.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: recovery review no new lecture. No new lecture or implementation feature. In the shared 25-minute study block, explain the existing security trace and recover only the first failing prerequisite; use the supplied instructor runtime and distinguish observed evidence from learner completion.
- Focused worked references: [Spring Security architecture — servlet architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Complete the task-specific proposal above before guidance; do not add a post-hoc architecture essay.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-63.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
