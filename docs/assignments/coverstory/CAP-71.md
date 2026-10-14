# CAP-71 — Deterministic text chunking

Implement `chunk_text` in `ai/coverstory/cap_71.py`. The synthetic public help text and expected codepoint offsets are in `fixtures/coverstory/CAP-71/synthetic-v1.json`. Learner contracts: `ai/coverstory/tests/test_cap_71.py`. Instructor oracle and defect witness: `ai/tests/test_coverstory_cap71_infrastructure.py`.

Run setup checks with `python3 -m unittest ai.tests.test_coverstory_cap71_infrastructure`; run learner cases separately with `python3 -m unittest ai.coverstory.tests.test_cap_71`. The current learner implementation intentionally remains a TODO.

Offsets count Unicode codepoints in Python strings. Each emitted text must equal its source slice exactly. This is chunk-boundary infrastructure for public synthetic payment-uncertainty explanations; it does not establish claim validity or payment state. Do not place protected consumer content in these fixtures.
