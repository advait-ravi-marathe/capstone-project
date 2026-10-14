# CAP-44 — Rank public insurance wording safely

The source-planned Python path `ai/coverstory/cap_44.py` is used. The public and restricted records are synthetic fixture entries; the `visible_to` fixture adapter demonstrates test principal scope and is not a production authorization policy.

## Instructor setup

- Typed source and result values plus the learner TODO: `ai/coverstory/cap_44.py`.
- Exact public, restricted, motor-scenario, and tie vectors: `fixtures/coverstory/CAP-44/synthetic-v1.json`.
- Learner acceptance suite: `ai/coverstory/tests/test_cap_44.py`.
- Independent setup check: `ai/tests/test_coverstory_cap44_infrastructure.py`.
- Run setup check: `python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap44_infrastructure.py'`.
- Run learner acceptance separately: `python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_44.py'`.

The contract covers public Visitor access, restricted-draft exclusion, category/scenario filtering, distinct-token overlap, deterministic tie order, exact excerpts, duplicate identity, and missing version rejection. The setup check verifies fixture content and its synthetic scope adapter; the current TODO acceptance run raises only from the unimplemented `rank_wording` method. Ranking and authorization behavior are not implemented. No timebox rehearsal has been performed.
