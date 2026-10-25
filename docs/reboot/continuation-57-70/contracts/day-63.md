# Day 63 — 2026-12-27: Run the security trace and recovery checkpoint

## Contract

**Learner-owned seam:** `Readiness evidence and request trace`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Review the authorization path end to end, record the prior evidence gate, and take the timed mock checkpoint. If security acceptance is still red, repair it inside the same engineering budget before any planned next task.

## Acceptance and expected TODO state

**Acceptance harness:** learner-owned bounded contract

Checks missing/null/false/true evidence, 0/60/85 minute preservation, rejection at -1 and 86, fail-closed recovery, and no new work in both branches. The learner seam remains TODO-red.

**Acceptance selector:** `day63SecurityCheckpointKeepsRecoveryInsideDailyBudget`
**Acceptance source:** `src/test/java/assignments/local/LocalReadinessGateAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day63SecurityCheckpointKeepsRecoveryInsideDailyBudget test
```

The selector is intentionally expected to fail at this day’s unchanged learner TODO until implementation evidence exists. This prospective harness is not completion credit.

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.

Keep unmet acceptance open as **S5-D63-RECOVERY**.
## Selector limits and open evidence

Checks missing/null/false/true evidence, 0/60/85 minute preservation, rejection at -1 and 86, fail-closed recovery, and no new work in both branches. The learner seam remains TODO-red. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-63](<../../assistant-architecture-notes-15-70.md#day-63>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: recovery review no new lecture. No new lecture or implementation feature. In the shared 25-minute study block, review the supplied security trace and recover only the first failing prerequisite; use the supplied instructor runtime and distinguish observed evidence from learner completion.
- Focused worked references: [Spring Security architecture — servlet architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Use the supplied assistant architecture note; record the actual selector/result or blocker and assistance.

Use the supplied decision as context for the assigned task; no design explanation is required.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner-owned changed paths in `evidence/continuation-57-70/day-63.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
