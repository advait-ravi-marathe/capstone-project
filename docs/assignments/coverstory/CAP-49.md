# CAP-49 — Classify source/version evidence failures

**Day:** 005 · 18 Oct 2026 · **Phase:** 1 · **Role:** linked component · **Component:** CAP-49

CAP-49 shares CAP-48's 150-minute day and adds zero minutes. Its fixture uses synthetic health wording only.

## Learner task

Implement `classify_failure(case, answer)` in [`ai/coverstory/cap_49.py`](../../../ai/coverstory/cap_49.py). Preserve the saved precedence: an unanswerable non-abstention is `UNSUPPORTED_ANSWER`; an answerable abstention is `FALSE_ABSTENTION`; then detect missing citation, wrong source, and wrong version, in that order. Exact expected source/version evidence passes. A correct abstention for an unanswerable case passes. This is a citation failure label, not evidence of factual, medical, or insurance suitability.

## Instructor scaffolding

- Synthetic precedence and outcome vectors: [`fixtures/coverstory/CAP-49/synthetic-v1.json`](../../../fixtures/coverstory/CAP-49/synthetic-v1.json).
- Learner acceptance: `ai/coverstory/tests/test_cap_49.py`.
- Independent reference and precedence mutants: `ai/tests/test_coverstory_cap49_infrastructure.py`.

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap49_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_49.py'
```

The independent oracle passes without calling the learner seam. Learner acceptance should stop at the preserved `NotImplementedError` until the learner implements the classifier.
