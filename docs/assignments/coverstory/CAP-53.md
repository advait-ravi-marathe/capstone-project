# CAP-53 — Load only approved synthetic wording files

The source-planned path `ai/coverstory/cap_53.py` is used. `fixtures/coverstory/CAP-53/corpus/manifest.json` is the instructor allowlist; only its direct child files may be read.

- Learner entry point: `load_wording(root: Path)` with immutable `PublicWording` and `LoadError` values.
- Acceptance suite: `ai/coverstory/tests/test_cap_53.py`; cases cover filename order, empty corpus, malformed/missing fields, path traversal with an outside-read spy, and duplicate version identity.
- Instructor smoke: fixture identity/scope check remains in the harness matrix; run the acceptance suite separately with `python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_53.py'`.

The loader is a TODO and performs no reads yet. No insurer documents or network access are included. The current acceptance test is expected to stop at the learner TODO. No timebox rehearsal has been performed.
