# Day 80 — 2027-01-11: Record fixed-cardinality preview outcomes

## Contract

**Learner-owned seam:** `PreviewMetrics.record`
**Status:** Contract defined; learner work not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

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

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no exact catalog match. No exact Micrometer timer/counter/cardinality lesson was found. Work fixed outcome tags, record timer/counter values on the actual route, and keep the meter count bounded; never tag with policy, owner or request IDs.
- Focused worked references: [Micrometer Timers — short-duration latency and event count](https://docs.micrometer.io/micrometer/reference/concepts/timers.html); [Micrometer Naming — Tag Values](https://docs.micrometer.io/micrometer/reference/concepts/naming.html#tag-values).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** List allowed metric dimensions and explain raw-ID cardinality.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-80.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 80` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
