# Actual learning Day 29: Targeted gap repair

**Status: Not started.** Planned date: **2026-11-24** (calendar planning only; a date never advances completion).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository. Select the intended learning position explicitly and record attempts only on their actual date.
- Role interview/AWS bank: 30 minutes.
- AI work: 90 minutes total, split below. No extra evidence or visibility time is scheduled today.
- Default total: **180 minutes**. If an actual DSA review consumes 75 minutes, reduce AI work to75; at90 minutes, reduce AI to60. Never add hours.

## Role practice (30 minutes)

Topic: **Targeted gap repair**. Reattempt the weakest prior topic with a fresh prompt; no arbitrary topic-volume quota. Use [the role bank and scoring rubric](../role-bank.md), record the actual prompt/time/answer and one specific follow-up. The score is not a hiring prediction.

## AI work (90 minutes)

### U021: failure taxonomy and trace records (30 minutes)

Finish the scheduled U021 slice: run the direct selector for your failure mapping and redacted evaluation-record builder. The deterministic selector checks mechanics; it does not create model observations. The pinned generation weights remain absent and the real evaluation gate is blocked. Do not fill evaluation rows with assistant/reference output or claim generated-answer quality. Record that gate as blocked and leave U021 incomplete until actual permitted inference and the assigned evaluation evidence exist.

### U022: bounded fixture paths (60 minutes)

Read the official [Python `pathlib` path resolution and `is_relative_to` documentation](https://docs.python.org/3/library/pathlib.html#pathlib.PurePath.is_relative_to) and OWASP's [Input Validation guidance](https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html#file-upload-validation). `resolve()` canonicalizes `..` and symlinks; compare the resolved file with the resolved fixture root. A filename or extension is untrusted input. This local exercise allows only `.json`, `.jsonl`, `.md` and `.txt` regular files under the supplied root, rejects symlink traversal and missing paths, and enforces a positive integer byte limit (default50,000).

Implement only `learner.validate_fixture_path(root, filename, max_bytes=50_000)`. Use the test fixture tree; do not add file upload, authentication, tenant access or public serving. Test a normal in-root file at the exact size limit, one byte over, traversal, absolute/outside/missing/directory paths, disallowed extension, NUL, symlinked file and symlinked directory, plus invalid limits. A symlink test may be skipped only if the filesystem cannot create symlinks; report that honestly.

**Acceptance command:**

```sh
python3 -m unittest ai.fde_v2.learner_acceptance.test_continuation_units.ContinuationAcceptance.test_u022_fixture_path_stays_inside_bounded_allowed_fixture_root
```

The reference witness is separately selected with `FDE_V2_CANDIDATE=ai.fde_v2.coverstory.reference`; it is not learner evidence. The test contains an independent constant-path mutant control.

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | Failure taxonomy and traces | 30 | [U021](../units/u021.md) |
| primary | Input, data and secret boundaries | 60 | [U022](../units/u022.md) |

## Evidence

Record the exact command, observed pass/fail, Python version and symlink-test availability. Keep filenames/content synthetic. Do not claim U021 or U022 complete from a reference witness, planned date, mock, or model that was not run.
