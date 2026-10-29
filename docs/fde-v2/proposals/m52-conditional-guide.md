# M52 conditional learning plan

**Status: proposal/scaffold only. Not activated and no learner work is recorded.**

**Status: Not started. Conditional design/scaffold only; prerequisite capability and learner evidence are not cleared.**

## Fixed 180-minute day

- DSA: 60 minutes, or DSA mock inside this block; record actual attempt date.
- Role/AWS bank: 30 minutes; preserve the existing bank and do not double-book formal assessment.
- AI: 90 minutes. Longer DSA or interview reallocations carry unfinished work forward.

Record actual date, minutes, command and output. A reference fixture, review receipt, course completion or elapsed day is not learner evidence. See the milestone proposal guide for gates and source links.

## Stage sequence

### M52-S01 — Dataset and metric contract

**Worked example and learner task:** Select lawful public or explicitly synthetic interaction data. Document source/license, schema, unit of prediction, candidates, relevance labels, metric and intended use.

**Acceptance/evidence:** Canned instructor fixtures test mechanics only. A learner-owned documented synthetic dataset may be used for the scoped offline exercise, but does not establish user or production quality.

### M52-S02 — Chronological split and baseline

**Worked example and learner task:** Sort by event time before splitting. The splitter returns boundaries but does not itself freeze them. Write a content-bound freeze artifact before evaluating; prevent future interactions leaking into training. Implement a popularity baseline from train only.

**Acceptance/evidence:** Assert max(train time) < min(validation time) < min(held-out time); record hashes.

### M52-S03 — Feature and leakage audit

**Worked example and learner task:** Inspect user/item identifiers and aggregates for future leakage. Build user-item vectors only from training interactions; define cold-start behavior.

**Acceptance/evidence:** No test-set-derived candidate construction or normalization.

### M52-S04 — Personal cosine ranker

**Worked example and learner task:** Learner implements cosine dot/norm math directly and scores candidate items from train history; no library fit/predict wrapper for the core scorer.

**Acceptance/evidence:** Check zero vectors, ties, seen-item exclusion and deterministic ranking.

### M52-S05 — Metric hand examples

**Worked example and learner task:** Work NDCG@K and one other justified ranking metric on a tiny hand-scored list. Freeze relevance interpretation and averaging.

**Acceptance/evidence:** No invalid empty-row or zero-IDCG values silently become a pass.

### M52-S06 — Development comparison

**Worked example and learner task:** Compare popularity and cosine on identical validation users/candidates using an explicit development view containing only train and validation rows plus a frozen-split marker. The comparison function rejects a full split object with held-out rows; derive relevance labels from validation only. Keep every unit and inspect cold-start/sparse-user failures.

**Acceptance/evidence:** Report paired counts, metric definitions, denominators and variance/sample size. A hand-scored non-perfect NDCG case must pass independently; a constant-one scorer must fail.

### M52-S07 — Frozen held-out scoring

**Worked example and learner task:** Freeze code/config/data and split hashes before opening held-out labels. Score once, retain per-unit outputs and analyze failures without tuning test.

**Acceptance/evidence:** Chronological hold-out, write-once model/data/split/config manifest and single-consumption run record required. Every held-out event has an output row including errors. Canned mechanics are not a learner result.

### M52-S08 — Local prediction API

**Worked example and learner task:** Serve one prediction with model/version metadata and strict type/size limits. Use learner ranker adapter in feature path.

**Acceptance/evidence:** Actual local request receipt required; missing trained artifact is BLOCKED.

### M52-S09 — Reproducibility and monitoring limits

**Worked example and learner task:** Record input/output/error/latency and model/data hashes; distinguish drift signals from quality change.

**Acceptance/evidence:** No online A/B, conversion uplift, production monitoring or time-saved claim.

### M52-S10 — Ranking depth gate

**Worked example and learner task:** Assemble source/split/model/evaluation/API/learner explanation and one failure analysis.

**Acceptance/evidence:** No GPU or paid compute needed; actual dataset/evaluation evidence may remain BLOCKED.

## Official short readings

### Stage reading anchors and runnable workflow

| Stages | Exact section or API heading |
|---|---|
| S01, S03 | [scikit-learn TimeSeriesSplit](https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.TimeSeriesSplit.html): class reference; Parameters; Examples |
| S02, S06, S07 | [TimeSeriesSplit](https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.TimeSeriesSplit.html): split method; test_size and gap parameters |
| S03, S04 | [scikit-learn cosine_similarity](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.pairwise.cosine_similarity.html): Parameters; Examples |
| S05, S06, S07, S10 | [scikit-learn ndcg_score](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.ndcg_score.html): Parameters; tie handling; Examples |
| S06, S09 | [scikit-learn model evaluation](https://scikit-learn.org/stable/modules/model_evaluation.html): Ranking metrics; NDCG scoring conventions |
| S08, S09 | [FastAPI request body and validation](https://fastapi.tiangolo.com/tutorial/body/): Request body; Path parameters and numeric validations |

The [write-once model bundle and held-out runner](../../../ai/fde_v2/m52/workflow.py) hash exact training rows, learner-code/config, chronological partitions, candidate catalog and metric cutoff. It records every held-out event once, including errors, and rejects changed inputs or replay. The command python -m unittest ai.fde_v2.tests.test_m52 includes partition tamper, candidate/metric drift and persisted bundle reload checks. The local split helper explicitly reports test_frozen false; only a valid freeze artifact establishes a frozen run. A local manifest is not a security boundary against its owner reading the held-out file.

- [scikit-learn cosine_similarity](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.pairwise.cosine_similarity.html), and [TimeSeriesSplit](https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.TimeSeriesSplit.html)
- [scikit-learn ndcg_score](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.ndcg_score.html)
- [Python statistics](https://docs.python.org/3.12/library/statistics.html)


The cosine similarity API is a reference for the mathematical definition only. The learner-owned ranker must implement dot product and vector norms in its own code; a framework `fit`/`predict` wrapper is insufficient. The scikit-learn NDCG page documents its averaging/tie behavior; local hand cases define this scaffold’s own scorer.

A passing scaffold check does not imply learner mastery, certification or professional experience; none is inferred from these artifacts.
No mastery or professional outcome is inferred from scaffold artifacts.
Professional experience is not inferred from this plan.
