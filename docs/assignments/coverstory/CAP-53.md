# CAP-53 — Load only approved synthetic wording files

The source-planned path `ai/coverstory/cap_53.py` is used. `fixtures/coverstory/CAP-53/corpus/manifest.json` is the instructor allowlist; only its direct child files may be read.

- Learner entry point: `load_wording(root: Path)` with immutable `PublicWording` and `LoadError` values.
- Acceptance suite: `ai/coverstory/tests/test_cap_53.py`; cases cover filename order, empty corpus, malformed/missing fields, path traversal with an outside-read spy, and duplicate version identity.
- Instructor reference: `ai/tests/test_coverstory_cap53_infrastructure.py` checks repeatable filename order, ignores unlisted files, rejects traversal before outside reads, rejects duplicate identities and restricted content, and fails atomically. Its test-only reference never calls `load_wording`.
- Run instructor controls: `python3 -m unittest ai.tests.test_coverstory_cap53_infrastructure`.
- Run learner acceptance separately: `python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_53.py'` (expected to stop at the learner TODO).

The loader is a TODO and performs no reads yet. No insurer documents or network access are included. The current acceptance test is expected to stop at the learner TODO. Day 009 instructor scaffold readiness and exact fixture/log evidence are in the [chronological readiness report](../../evidence/coverstory/phase1-days-001-010/README.md); no elapsed-time claim is made.
