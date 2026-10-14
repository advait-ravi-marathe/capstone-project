# CAP-73 — Separate development and held-out evaluation sets

The learner seam at `ai/coverstory/cap_73.py` is `validate_split(dev, golden)`. It remains intentionally unimplemented. The synthetic fixture covers a clean split plus case-ID, normalized-question and source-family leakage, and an empty golden set.

The instructor oracle at `ai/tests/test_coverstory_cap73_infrastructure.py` independently checks these integrity rules. Normalization is lowercase plus collapsed whitespace. No model, threshold tuning, real health/payment record, or golden-label evaluation is involved. Distinct case IDs are insufficient when wording or source lineage overlaps.

## Commands

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap73_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_73.py'
```

The learner suite should stop at the explicit `NotImplementedError` until the assignment is attempted. Passing instructor fixtures verifies the harness only; it does not demonstrate model quality or independent mastery. Golden labels must stay outside model/threshold tuning.
