# Day 81 — 2027-01-08: Show an accessible manual retry state

## Contract

**Learner-owned seam:** `RenewalPreviewBusyState`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Integrate the accessible `RenewalPreviewBusyState` in the normal `LocalCoverStoryApp` route. Show busy/unavailable status, retain policy context, and expose one explicit retry only; do not auto retry or reset session.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Integrated learner acceptance test:** `frontend/src/__tests__/LocalCoverStoryApp.test.tsx`, case `shows the real renewal route busy state and retries only after the user activates Retry`.
**Integrated command:** `cd frontend && RUN_LEARNER_ACCEPTANCE=true npx vitest run src/__tests__/LocalCoverStoryApp.test.tsx -t 'shows the real renewal route busy state and retries only after the user activates Retry'`
**Focused component acceptance test:** `frontend/src/components/RenewalPreviewBusyState.test.tsx`.
**Focused command:** `cd frontend && RUN_LEARNER_ACCEPTANCE=true npx vitest run src/components/RenewalPreviewBusyState.test.tsx`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

RenewalPreviewBusyState.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no direct course match. No exact accessible manual-only retry lesson was verified. Sketch retained policy context and mutually exclusive UI state. Verify no automatic retry/refetch/session reset; only an explicit Retry action invokes the operation. TanStack cancellation is not a substitute for manual-only semantics.
- Focused worked references: [WAI-ARIA 1.2 — status role and alert role](https://www.w3.org/TR/wai-aria-1.2/#status).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Draw state transitions and prove retry follows explicit action.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-81.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-08`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
