# Day 80 — 2027-01-07: Record fixed-cardinality preview outcomes

## Contract

**Learner-owned seam:** `PreviewMetrics.record`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Record finite timer/counter outcomes through the bounded store used by the real renewal route; no IDs as tags; meter count cannot grow with request or policy cardinality.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewMetricsAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewMetricsAcceptanceTest test`
**Observability infrastructure test:** `src/test/java/assignments/local/LocalMetricsExposureInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=LocalMetricsExposureInfrastructureTest test`
**Batch contract-integrity test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Batch command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

Connect `PreviewMetrics` to `BoundedPolicyRenewalReadStore` and the local singleton configuration. Each real route outcome contributes a finite `outcome` tag and bounded timer/counter data.

For the packaged synthetic local demo, `GET /actuator/metrics` is an explicit read-only observability endpoint. It is available only in the `local` profile, the application enforces loopback binding, Spring Security requires the synthetic owner session, and a Micrometer allow-list exposes only `coverstory.preview.requests`, `coverstory.preview.latency`, and `hikaricp.connections.active`; route tags are limited to the fixed outcome enum. It is not a business endpoint or a fault-injection route. The HTTP endpoint itself is instructor-owned configuration; learner work is recording the route metrics.

## Concept/course allocation (25 minutes)

Read the primary source for 10 minutes; sketch the invariant/data flow for 10 minutes; teach it back in 5 minutes. These are planner allocations, not setup tasks.

- [Micrometer Timers](https://docs.micrometer.io/micrometer/reference/concepts/timers.html)
- [Micrometer Naming — Tag Values](https://docs.micrometer.io/micrometer/reference/concepts/naming.html#tag-values)

**Design sketch:** List allowed metric dimensions and explain raw-ID cardinality.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-80.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-07`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
