# M2.1 conditional draft scaffold verification

**Status: proposal and deterministic mechanics scaffold only.** This does not activate M2.1, complete a learning day, satisfy P1, verify human labels, or pass a release evaluation. The learner seams in `ai/fde_v2/m21/learner.py` remain `NotImplementedError` TODOs. The accepted milestone contract remains unchanged.

## Scope added

- `docs/fde-v2/proposals/m21-detailed-days-42-62.md` and `ai/fde_v2/m21/draft_schedule.json` map a proposed 21-session design to accepted `AIFDE-V2-M2.1`. The schedule preserves 60 DSA + 30 role/AWS + 90 AI minutes per nominal day, 1,500 substantive M2.1 minutes, 240 actual buffer minutes, 90 visibility minutes inside AI, and 60 expressly unassigned/outside-M2.1 minutes on Day 62.
- `ai/fde_v2/m21/learner.py` defines unsolved learner seams. `reference.py` is an explicit instructor-only mechanics witness; no default application path imports it.
- `ledger.py` is an assistant-owned SQLite sidecar for staging/version/tombstone mechanics. `coverstory/vector_store.py` gains local point-version inspection/deletion primitives. This is not a production migration or claim that P1 currently uses the sidecar.
- `ai/fde_v2/learner_acceptance/m21_acceptance.py` defines proposed selectors whose default candidate is the unsolved learner. `test_m21_draft.py` explicitly swaps in the instructor reference only for deterministic witnesses and mutation checks.
- The main roadmap README links the proposed design as inactive and corrects continuation-branch status; accepted contracts, learner records, DSA content and board data are unchanged.

## Deterministic verification

Run from repository root after the existing v2 dependencies are installed:

```sh
PYTHONPATH=/tmp/capstone-v2-test-deps:/tmp/capstone-qdrant-libs${PYTHONPATH:+:$PYTHONPATH} \
  python -m unittest scripts.test_fde_v2_m21_draft ai.fde_v2.tests.test_m21_draft -v
```

Result: **19 tests passed**. The witnesses cover proposal/accepted-contract parity, exact time arithmetic, synthetic 100-row split mechanics including row-label/manifest mismatch rejection, staged update and SQL-head preservation, restart observation, stale-point filtering after cleanup failure, tombstone-before-delete, Qdrant point deletion/reconciliation, retrieval/latency metric edge cases, exact paired-case comparison, frozen-config no-overwrite, the proposed threshold arithmetic and material split/metric mutants. Unknown answerability labels are rejected rather than scored as abstentions. A synthetic complete-shape fixture exercises the final evaluation-gate route and can produce only `ELIGIBLE_FOR_INDEPENDENT_REVIEW` with `m21_pass: false`; a request failure cannot receive a successful human judgment. These fixture labels and results are not actual P1/model evidence. The nine learner selectors were separately invoked with the unsolved learner module and all nine stopped at their explicit TODOs; selectors passed only when the instructor reference was explicitly substituted.

Additional checks on this worktree:

- `PYTHONPATH=... bash scripts/fde_v2_clean_check.sh` — passed, including Sprint 1/continuation suites, M2.1 draft tests and the two FDE Vitest selectors (11 tests each).
- `PYTHONPATH=... python -m unittest discover -s scripts -p 'test_*.py' -v` — **225 tests passed**.
- `npm --prefix frontend run build` — passed.
- `npm --prefix frontend test` — passed: 24 Vitest tests and one Node test.
- `git diff --check` — passed.

The CI workflow now runs both M2.1 draft suites after the existing Sprint 2 dependency installation. Exact-head remote CI status is not claimed until the branch push completes and the remote reports it.

## Evidence still blocked

P1 remains uncleared. No model metadata request or weights download was made for this work. There are no verified model weights or real outputs in the evidence bundle. Fixed synthetic vectors, locally persisted Qdrant records, fabricated mechanics fixtures, reference code, green tests, file presence, and elapsed time do not count as actual embedding/generation, source-verified labels, learner completion, or M2.1 evaluation evidence. The proposed 100-case dataset, development/held-out gold, annotation pilot and release measurements have not been authored or validated.

The next accepted outcome contract is M2.2. Its separate conditional design and deterministic mechanics now live in `proposals/m22-detailed-days-62-82.md`; that does not satisfy M2.1 prerequisites, activate M2.2 or make its proposed days active. No fixed day/date for M2.1 completion is claimed; the Day42–62 labels remain conditional learning-session indices only.
