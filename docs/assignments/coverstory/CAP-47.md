# CAP-47 — Evaluate citation support and abstention

**Day:** 004 · 17 Oct 2026 · **Phase:** 1 · **Role:** linked component · **Component:** CAP-47

CAP-47 shares CAP-46's 150-minute day and adds zero minutes. It evaluates only source/version citation support and explicit abstention on synthetic data. It does not evaluate medical or insurance suitability, factual correctness, or semantic answer quality.

## Learner task

Implement `evaluate(cases, answers)` in [`ai/coverstory/cap_47.py`](../../../ai/coverstory/cap_47.py). Require exactly one answer for each case. An answerable case is supported only when it is not marked abstained, cites at least one allowed `(source_id, version)` pair, and contains no foreign pair. An unanswerable case is a correct abstention only when `abstained=true` and there are no citations. Every other combination is unsupported. Missing or extra answer IDs raise `ValueError`.

## Instructor scaffolding

- Synthetic records and expected totals: [`fixtures/coverstory/CAP-47/synthetic-v1.json`](../../../fixtures/coverstory/CAP-47/synthetic-v1.json).
- Learner acceptance suite: `ai/coverstory/tests/test_cap_47.py`.
- Independent reference, fixture and mutant checks: `ai/tests/test_coverstory_cap47_infrastructure.py`.

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap47_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_47.py'
```

The infrastructure oracle is independent of `evaluate`; the learner suite should reach the explicit TODO until implemented.
