# CAP-69 — Compare direct rejection with a bounded local queue

## Learner seam and exact source paths

Implement `summarize` and `validate_claim` in [cap_69.py](../../../ai/coverstory/cap_69.py). The typed seam is `LoadRun -> QueueDecisionEvidence`; the synthetic trace is [synthetic-v1.json](../../../fixtures/coverstory/CAP-69/synthetic-v1.json). Learner acceptance cases are [test_cap_69.py](../../../ai/coverstory/tests/test_cap_69.py). The independent instructor witness is [test_coverstory_cap69_infrastructure.py](../../../ai/tests/test_coverstory_cap69_infrastructure.py).

Run instructor setup separately:

```sh
python3 -m unittest ai.tests.test_coverstory_cap69_infrastructure
```

Run learner acceptance separately; `NotImplementedError` at the two explicit TODOs is expected until the learner implements the seam:

```sh
python3 -m unittest ai.coverstory.tests.test_cap_69
```

## Contract

This is a deterministic simulation only. Twenty synthetic tasks arrive at once; a bounded in-process FIFO has capacity 10 including the active worker, and service rate is 2 tasks/second. The bounded case accepts 10, rejects 10, completes 10, reaches queue maximum 10, and drains in 5000 ms. For a comparable direct-rejection baseline, there is no waiting room: one task offered while the single worker is idle is accepted and the other 19 are rejected. The fixture defines p95 as nearest-rank completion latency for accepted work (5000 ms for bounded queueing; 500 ms for direct rejection); with zero offered tasks, p95 is null and drain is zero. Capacity zero, missing/unbounded capacity, or nonpositive rate returns `INVALID_CONFIG`.

The trace compares direct rejection (no waiting room: an offer arriving while the single worker is occupied is rejected) with bounded queueing. Queueing changes when admitted work runs; rejected work remains visible. Validate claims against the evidence and reject a claim that the queue eliminated overload while ten tasks were rejected. Do not add Kafka, a runtime queue, or a capacity claim about production.

## Instructor setup and learner ownership

The instructor oracle and fixture checks are independent of the learner implementation. They contain a bounded-capacity defect witness and an overload-claim defect witness. Learners own simulation logic, input validation, evidence interpretation, and the conclusion about whether local queueing is justified. Passing instructor checks validates only the synthetic harness; it does not establish learner completion or production throughput.
