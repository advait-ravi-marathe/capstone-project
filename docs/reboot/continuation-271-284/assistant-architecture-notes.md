# Assistant-supplied architecture notes

The assistant chooses the architecture and supplies concise what/why context. The learner implements and tests assigned seams. System-design practice remains only in separate interview question-bank sessions.

The PostgreSQL experiment uses pinned PostgreSQL 18.6, a deterministic synthetic seed, loopback-only disposable instance, EXPLAIN JSON plus exact result readback, and a pure analyzer compared to independent expected facts. Planner shape, cost, buffer counts and elapsed time may vary with cache/statistics/host; correctness is determined by exact rows and analyzer arithmetic, not timing. JFR is a separate bounded 500-million-iteration local CPU workload. Default recording settings avoid adding event volume; sampled events can miss brief work and recording itself has overhead. Neither rehearsal represents service performance, production operation or learner completion.

<a id="day-271"></a>
## Day 271 — Capture a deterministic SQL baseline

Pin PostgreSQL 18.6 and deterministic seed data; bind only to loopback and capture to ignored target/ so fresh observations never overwrite the canonical fixture.

<a id="day-272"></a>
## Day 272 — Verify the workload's relational facts

Derive the expected composite identities from the seed arithmetic, independently of planner output; exact order and tenant keys prevent a same-count leak from passing.

<a id="day-273"></a>
## Day 273 — Implement exact result comparison

Compare complete ordered composite identities; neither count-only nor order-insensitive checking can establish that the intended revisions were read.

<a id="day-274"></a>
## Day 274 — Compare estimated and actual cardinality

Use a symmetric finite ratio for nonzero counts and explicit zero states; avoid undefined division and do not turn q-error into a speed or correctness score.

<a id="day-275"></a>
## Day 275 — Apply the analyzer to a skewed workload

Add a known 4,000-row owner skew, capture before and after ANALYZE, then feed the unchanged policy query through the completed learner analyzer. Keep plan estimates observational: statistics refresh does not promise a particular node or estimate.

<a id="day-276"></a>
## Day 276 — Validate malformed plan evidence

Validate types and finite ranges before interpreting plan data; report nested scan estimates from the skew artifact without hard-coding a plan-tree shape or requiring statistics to improve estimates.

<a id="day-277"></a>
## Day 277 — Compare plan and answer independently

Use a fresh disposable database with only the index layout changed; the answer rows must stay exact while the plan/cost/buffers may differ.

<a id="day-278"></a>
## Day 278 — Record a bounded JVM profile

Use a fixed 500-million-iteration CPU loop to provide a bounded sustained phase while keeping work identity deterministic. JFR defaults limit event volume; sampling and recording overhead remain variable, and this local loop is not a service profile.

<a id="day-279"></a>
## Day 279 — Inspect selected recording events

Use an enabled custom JFR phase event as a deterministic recording-integrity marker; inspect its workload fields and summary, then report sampled ExecutionSample data separately because counts vary with host and sampling intervals.

<a id="day-280"></a>
## Day 280 — Relate runtime observations to workload

Use separate JVMs and recordings so samples do not share process state; require identical 500M custom markers and checksums, allow sampled event counts to differ, and avoid comparing wall-clock duration.

<a id="day-281"></a>
## Day 281 — Re-run acceptance mutations

The instructor reference remains isolated from the learner starter; independent expected facts and mutations protect the real assertion path without awarding learner credit.

<a id="day-282"></a>
## Day 282 — Explain a planner counterfactual

Change only multicolumn key order in a clean database and keep the exact query/result fixed; both equality predicates are present, so conclusions remain workload-specific.

<a id="day-283"></a>
## Day 283 — Reproduce and compare the observation

Rebuild the same seed and ANALYZE state in a fresh instance; correctness must transfer while volatile planner/cache observations are reported, not normalized away.

<a id="day-284"></a>
## Day 284 — Review gates and transfer the diagnosis

Keep the same baseline variant and exact answer across three fresh local runs; summarize median and spread to teach Python statistics, but treat tiny same-host timings as descriptive, not a service SLO.
