# Day 81 — 2027-01-12: Show an accessible manual retry state

## Contract

**Learner-owned seam:** `RenewalPreviewBusyState`
**Status:** Contract defined; learner work not attempted.

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

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
