# Day 63 — 2026-12-21: Run the security trace and recovery checkpoint

## Contract

**Learner-owned seam:** `Readiness evidence and request trace`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Review the authorization path end to end, record the prior evidence gate, and take the timed mock checkpoint. The required `day-62-security` evidence controls recovery. `implementationMinutes` is remaining practice time: accept and return any value from 0 through 85 unchanged; reject values outside that range. Missing, null or false evidence requires recovery; true avoids recovery. Both branches keep `newWorkAdded=false`. If security acceptance is still red, repair it inside the same engineering budget before any planned next task.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.

**Acceptance test:** `src/test/java/assignments/local/LocalReadinessGateAcceptanceTest.java`
**Acceptance selector:** `day63SecurityCheckpointKeepsRecoveryInsideDailyBudget`
**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day63SecurityCheckpointKeepsRecoveryInsideDailyBudget test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**

- No course video is required. Use the official references and complete worked example below.

**Study sequence (seconds; total 1500):**
- retrieve taught security boundary: 480 seconds.
- first-failure worked diagnosis: 420 seconds.
- timed trace and debrief: 600 seconds.

**Official references:**
- [Spring Security: Servlet Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html) — Authentication

**Worked guide:** [day-63.md](../worked/day-63.md). Read the complete supplied reference before applying the distinct native variation. This is instructor teaching, not learner completion.

**Time rule:** Optional lessons replace time inside these 1500 seconds; pauses do too. No overtime. Access and viewing are not inferred.

**Runnable acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day63SecurityCheckpointKeepsRecoveryInsideDailyBudget test`

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-63.json`. Leave completion and actual dates blank until the work is actually done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 63` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
