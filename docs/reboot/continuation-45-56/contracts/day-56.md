# Day 56 — 2026-12-14: Prove preview stability across restart without writes

## Contract

**Learner-owned seam:** `read-only preview integration`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Repeat the same browser read after restarting Spring. The response snapshot is identical and PostgreSQL lifecycle row count is unchanged; GET is read-only.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-56.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/renewal/RenewalPreviewTransportInfrastructureTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Dtest=RenewalPreviewTransportInfrastructureTest#testOnlyReadOnlyBrowserTransportSurvivesSpringRestart test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: integration review no new lecture. No exact course lesson verifies persistence across packaged restart or database no-write behavior. Use the supplied disposable packaged-browser harness; compare stable preview JSON and complete PostgreSQL snapshots before and after restart. Course viewing is unnecessary.
- Focused worked references: [Spring Boot application testing — real application and server testing](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html); [PostgreSQL transaction isolation — read-only observations and statement snapshots](https://www.postgresql.org/docs/14/transaction-iso.html).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Prove preview stability across restart without writes: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-56.json`. Record first-attempt and review dates only after the work is done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-12-14 --roadmap-day 56` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
