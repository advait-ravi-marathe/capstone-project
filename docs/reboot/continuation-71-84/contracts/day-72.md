# Day 72 — 2026-12-30: Validate finite preview capacity policy

## Contract

**Learner-owned seam:** `PreviewCapacityPolicy.validate`
**Status:** Contract defined; learner work not attempted. The configured local runtime invokes `validate()` before serving requests.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Require positive counts, permits <= pool maximum, and finite connection timeout 250..30000ms.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewCapacityPolicyAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewCapacityPolicyAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

PreviewCapacityPolicy.validate.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no exact catalog match. No exact Hikari configuration validation lecture was found. Work a table requiring positive permits ≤ pool maximum and a finite 250–30000 ms acquisition timeout; keep acquisition timeout distinct from SQL/lock timeout.
- Focused worked references: [HikariCP configuration knobs — connectionTimeout and maximumPoolSize](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Sketch the relationship between two preview permits and a four-connection pool, then explain why this does not reserve two pool connections. The 250..30000ms connection timeout interval is project validation policy, not a Hikari maximum.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-72.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-30`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
