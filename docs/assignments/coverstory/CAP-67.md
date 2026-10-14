# CAP-67 — Recall, reciprocal rank, and honest denominators

## Learner-owned seam

Implement `metrics_at_k` and `aggregate` in `ai/coverstory/cap_67.py`. The synthetic source is `fixtures/coverstory/CAP-67/synthetic-v1.json`; learner acceptance is `ai/coverstory/tests/test_cap_67.py`. The independent instructor witness is `ai/tests/test_coverstory_cap67_infrastructure.py`.

Run setup checks with `python3 -m unittest ai.tests.test_coverstory_cap67_infrastructure`. Run the learner contract separately with `python3 -m unittest ai.coverstory.tests.test_cap_67`; current TODO failures are expected.

## Contract

Recall@k is the fraction of distinct relevant IDs present in the top-k result. Reciprocal rank uses the first relevant ID's one-based rank in the complete ranking. Duplicate ranked IDs and invalid k are rejected. Empty relevance is not applicable and has null metrics; aggregates average only applicable cases and report their denominator explicitly.

## Safety and interpretation

The fixture is wholly synthetic and evaluates ranking behavior, not health advice quality or clinical safety. A mean without the answerable count is not a complete evaluation. Passing instructor checks establishes the fixture and oracle behavior only; it does not complete the learner seam or prove independent mastery.
