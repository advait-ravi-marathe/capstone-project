# Day 71 — 2027-01-02: Establish preview contention baseline and verify prerequisites

## Contract

**Learner-owned seam:** `Record the existing synchronous path and fixed pool ceiling; no runtime feature.`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Baseline the read route and identify unfinished ownership/readiness prerequisites.

### Acceptance and expected TODO state

This is an instructor evidence/contract check, not a learner implementation acceptance test. Record learner implementation and integration outcomes separately; do not infer completion from this selector.

**Instructor baseline check:** `src/test/java/assignments/renewal/Day71BaselineAcceptanceTest.java`
**Instructor command:** `./mvnw -Dtest=Day71BaselineAcceptanceTest test`
**Batch contract-integrity check:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Batch command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

Record the existing synchronous path and fixed pool ceiling; no runtime feature..

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: evidence review no new lecture. No exact Hikari sizing/acquisition lecture was found. Work a two-admission/four-connection request timeline and mark which waits are finite.
- Focused worked references: [HikariCP configuration knobs — maximumPoolSize and connectionTimeout](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Record the request path, one overload failure and the layer that must bound it.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-71.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
