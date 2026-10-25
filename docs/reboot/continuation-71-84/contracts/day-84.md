# Engineering unit Day 84 · Calendar study day 98 · 2027-01-31: Integrate recovery evidence and independent assessment

## Contract

**Learner-owned seam:** `Independent evidence review; no feature`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Run installed selectors; record known TODOs/blockers; explain bounds/recovery; keep outcome axes separate.

### Acceptance and expected TODO state

This is an instructor evidence/contract check, not a learner implementation acceptance test. Record learner implementation and integration outcomes separately; do not infer completion from this selector.

**Instructor contract-integrity test:** `src/test/java/assignments/renewal/Day84ContractInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=Day84ContractInfrastructureTest test`
**Batch contract-integrity test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Batch command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Practice allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

Independent evidence review; no feature.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: assessment review no new lecture. No feature or course. Explain capacity/acquisition/query-wait boundaries, safe error mapping and independent evidence axes using observed selector results; unresolved prerequisites become recovery work.
- Focused worked references: [Java jcmd — thread dump diagnostics](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Assistant-provided architecture note:** [Day 84](../assistant-architecture-notes.md#day-84). Apply the stated design to the existing seam. No capstone design proposal or post-hoc architecture justification is assigned.

**Retrieval check:** Name the existing boundary and selector that observes it. This recalls supplied guidance; it is not a new design proposal or justification.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-84.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
