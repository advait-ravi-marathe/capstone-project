# CAP-59 — Verify exact citation provenance

The source-planned path `ai/coverstory/cap_59.py` is used. The registry fixture contains only one synthetic source/version and explicitly marks authorization and applicability.

- Learner seam: `verify_citation(Citation, registry)`.
- Fixture vectors: `fixtures/coverstory/CAP-59/synthetic-v1.json`.
- Learner suite: `ai/coverstory/tests/test_cap_59.py`.
- Independent fixture check: `ai/tests/test_coverstory_cap59_infrastructure.py`.
- Run the focused suites with Python unittest discovery for `test_coverstory_cap59_infrastructure.py` and `test_cap_59.py`.

The vectors distinguish exact source/version lookup, Python code-point offsets, empty excerpts, unknown source/version, authorization/applicability, excerpt mismatch, and digest tampering. Hash validity does not grant source authority or mean a source is a payable quote. The verifier remains TODO; no latest-version fallback is provided by the fixture.
