# CAP-56 — Measure retrieval without hiding failures

**Learner outcome:** Measure a reproducible source-wording baseline while preserving unanswerable and denied-source failures.

- Learner seam: `evaluate_retriever` in `ai/coverstory/cap_56.py`.
- Synthetic evaluation vectors: `fixtures/coverstory/CAP-56/synthetic-v1.json`.
- Learner contract suite: `ai/coverstory/tests/test_cap_56.py`.
- Independent setup suite: `ai/tests/test_coverstory_cap56_infrastructure.py`.

The fixture pins SHA-256 over canonical compact JSON projections of the synthetic cases (sorted object keys; UTF-8) and the evaluation configuration. The expected metric is macro hit@2 over answerable queries only; an all-unanswerable sample reports a null metric and explicit counts. A result outside the authorized source set fails the whole run. Repeated runs over the same fixture/config produce the same measured report. Timestamps are intentionally omitted from this deterministic comparison.

The implementation remains a learner TODO. The instructor check validates fixture contents and recomputes both digests; it does not assess learner behavior or certify a real retriever. Run the acceptance suite separately; expected `NotImplementedError` failures indicate the untouched learner seam only.

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap56_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_56.py'
```
