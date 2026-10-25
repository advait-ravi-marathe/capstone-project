# Engineering unit Day 72 · Calendar study day 86 · 2027-01-19: Validate finite preview capacity policy

## Contract

**Learner-owned seam:** `PreviewCapacityPolicy.validate`
**Status:** Contract defined; learner work not attempted. The configured local runtime invokes `validate()` before serving requests.

### Expected behavior

Require positive counts, permits <= pool maximum, and finite connection timeout 250..30000ms.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewCapacityPolicyAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewCapacityPolicyAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Practice allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

PreviewCapacityPolicy.validate.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no exact catalog match. No exact Hikari configuration validation lecture was found. Work a table requiring positive permits ≤ pool maximum and a finite 250–30000 ms acquisition timeout; keep acquisition timeout distinct from SQL/lock timeout.
- Focused worked references: [HikariCP configuration knobs — connectionTimeout and maximumPoolSize](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Assistant-provided architecture note:** [Day 72](../assistant-architecture-notes.md#day-72). Apply the stated design to the existing seam. No capstone design proposal or post-hoc architecture justification is assigned.

**Retrieval check:** Name the existing boundary and selector that observes it. This recalls supplied guidance; it is not a new design proposal or justification.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-72.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
