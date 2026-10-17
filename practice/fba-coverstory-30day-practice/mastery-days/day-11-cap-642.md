# Day 11 · 2028-06-28 · CAP-642 / CSM14-11

**Board contract:** [CAP-642](https://dwait-to-adwait.youtrack.cloud/issue/CAP-642) — CSM14-11 · 28 Jun 2028 — Measure bounded refresh-history query behavior (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-03, CSM14-05, CSM14-10

## Contract focus

Implement or refine the owning Shortlist history/read adapter to return a bounded 20-revision page and fetch at most three current source references through the Catalog public batch port. Eliminate only a demonstrated N+1 or unnecessary scan in this new checkpoint path. Compare one narrow index/query alternative against the baseline using the prepared identical synthetic datasets and supported PostgreSQL plan tools.

## Synthetic fixture

Prepared datasets contain 100 and 1,000 immutable checkpoint revisions for A, matching rows for B, and three public source families. Page size is 20 with deterministic revision ordering. Each before/after run uses the same dataset, candidate settings, warmup and request accounting; at most five measured executions per condition. This is a local synthetic experiment, not load testing a live service.

## Acceptance checklist

- [ ] The page contains only A’s requested checkpoint revisions with stable order and no repeated/missing rows in the fixed fixture.
- [ ] Public-port call counters prove the declared bounded lookup count; private ownership is not dropped to obtain a faster plan.
- [ ] Save actual EXPLAIN/query metrics, rows examined/returned and all failure counts for matched runs; label cold/warm conditions and tiny sample limits.
- [ ] The chosen optimization must retain correctness and show its measured trade-off, including no improvement if that is the result.
- [ ] No distributed cache, new database, broker or paid infrastructure is introduced to satisfy this bounded workload.

## Architecture/evidence

Write a brief measured decision record with workload, query count, actual plan observation, storage/write cost of the candidate index and a falsifying workload. Do not extrapolate 1,000 local rows to production capacity.

## Scope and provenance

The measurement is attached to the newly implemented branch-checkpoint history API, not a generic cache cost model or misleading-benchmark review.

Structured contract hash: `5af78fd67ec7140ac771d8fca7ff6d1a26770d232405122ff285108728057b31`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap642_historyIsOwnerBoundAndPageIsCappedAtTwenty test`
Instructor baseline: `./mvnw -Dtest=CSM14CheckpointSchemaIntegrationTest#ownerBoundHistoryQueryReturnsStableTwentyRowPageFromSyntheticThousand test`. Add the matched baseline/candidate index comparison and measured interpretation as learner work; no production-scale claim.
