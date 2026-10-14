# CAP-61 — Gate extractive answers by evidence threshold

The source-planned path `ai/coverstory/cap_61.py` is used. Scores are supplied evaluation scores and must not be presented as calibrated confidence.

- Learner seam: `decide_answer(authorized_hits, threshold)`.
- Typed fixture values and exact vectors: `fixtures/coverstory/CAP-61/synthetic-v1.json`.
- Learner acceptance suite: `ai/coverstory/tests/test_cap_61.py`.
- Independent fixture check: `ai/tests/test_coverstory_cap61_infrastructure.py`.
- The independent setup suite is a separate unittest file; learner behavior remains tagged by its focused `test_cap_61.py` suite.

Tests cover inclusive threshold boundaries, extractive text/citation preservation, denied/inapplicable/empty abstention, and NaN/out-of-range rejection. This is a synthetic evaluation gate; score alone cannot change an unknown insurance field. The implementation remains TODO and is not a calibrated production model.
