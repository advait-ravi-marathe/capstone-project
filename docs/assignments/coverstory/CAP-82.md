# CAP-82 — Compare grounded source rankers safely

## Learner seam and paths

Implement `compare_runs(baseline, candidate)` in [cap_82.py](../../../ai/coverstory/cap_82.py). Typed `EvalRun` and `EvaluationCase` inputs carry the immutable dataset/corpus identity, k, labeled development cases, per-case ordered source IDs, ownership metadata, and observed latency samples. Synthetic inputs live in [synthetic-v1.json](../../../fixtures/coverstory/CAP-82/synthetic-v1.json); acceptance tests are [test_cap_82.py](../../../ai/coverstory/tests/test_cap_82.py); independent instructor checks are [test_coverstory_cap82_infrastructure.py](../../../ai/tests/test_coverstory_cap82_infrastructure.py).

Dependencies reviewed: CAP-56 establishes the read-only synthetic retrieval fixture and fails closed on unauthorized sources; CAP-79 filters stale source evidence; CAP-81 separates authoritative owner-linked evidence from lossy analytics. Their contracts are not reimplemented here. This comparison consumes fixed ranker outputs and must not tune the rankers, labels, or sources.

Run setup and learner checks separately:

```sh
python3 -m unittest ai.tests.test_coverstory_cap82_infrastructure
python3 -m unittest ai.coverstory.tests.test_cap_82
```

The five learner cases currently stop at the explicit TODO until comparison logic is implemented.

## Contract

Compare only when dataset hash, corpus hash, k, case identities and answerability/relevance labels match. For answerable cases, report macro mean reciprocal rank, macro recall@k, their candidate-minus-baseline deltas, per-run mean latency from the supplied samples, and case IDs whose quality metrics changed. Keep negative deltas. The first fixture produces baseline MRR 0.50 and candidate 0.75 (delta +0.25), with `q2` identified. The regression fixture retains delta -0.25.

Different dataset identity returns `INCOMPARABLE` with no gain metrics. Any returned private source owned by a different query owner returns `DISQUALIFIED` with no quality delta, regardless of rank gain. An empty answerable set returns `INSUFFICIENT_EVIDENCE` with null quality metrics. The fixture latency values are synthetic supplied observations for checking the report; label them fixture-only. Do not describe them as live runtime measurements.

## Learner ownership and limits

Learners own comparison math, changed-case reporting, identity checks, and safety disposition. They do not tune rankers, adjust relevance labels, access held-out labels, retrieve private application documents, or convert ranking into health/premium advice. Instructor witnesses include a gain-only defect that ignores dataset identity and cross-owner authority; the correct oracle rejects both. Passing setup tests checks the synthetic contract only and does not establish independent mastery or retrieval quality on real-world health sources.
