# CAP-44 — Applied AI: retrieval ranking + source provenance

**Date:** 14 Oct
**Timebox:** 60 minutes
**Runnable status:** the independent starter and its checks are runnable. CAP-41
comparison is pending until a CAP-41 baseline exists.

## Goal

Rank tied evidence deterministically while retaining immutable source
provenance. Do not fabricate similarity or confidence values.

## Learner task

Implement `rank_runbooks(query: str, owner_id: str, runbooks: list[Runbook],
k: int) -> list[RankedHit]` in `ai/retrieval/ranking.py`. Instructor-provided
`Runbook`, `RankedHit`, `tokenize`, and `score_runbook` are not learner work.

The tokenizer lowercases text, extracts ASCII alphanumeric tokens using
`[a-z0-9]+`, and treats them as a set. Score each eligible runbook by the
number of distinct query tokens found in its text tokens. Apply exact owner
authorization before scoring: only entries whose `allowed_owners` contains the
exact `owner_id` may be scored. Exclude zero-score entries. Sort by score
descending, then `source_id` ascending, then `version` ascending. Return at
most `k` hits, preserving exact source ID, version, and full text as excerpt.

Blank or whitespace-only queries return an empty list. Blank owner IDs, `k`
outside 1 through 3, missing/blank source ID or version, and duplicate
`(source_id, version)` pairs raise `ValueError` before returning partial
results. Do not trim or rewrite provenance or excerpts.

## Fixed examples

For owner `A`, query `retry timeout`, and `k=3`:

| source | version | owners | text | expected score |
| --- | --- | --- | --- | ---: |
| r2 | v1 | A | `retry` | 1 |
| r1 | v1 | A | `retry timeout` | 2 |
| r3 | v2 | A | `timeout` | 1 |
| r0 | v1 | B | `retry timeout` | excluded before scoring |

Expected order: `r1/v1`, `r2/v1`, `r3/v2`. Reversing the allowed input rows
must produce the same order. Query `billing` and query ` ` each return `[]`.
Duplicate `r1/v1` or a missing version raises `ValueError`.

## Run and verify

From the repository root, Python 3.9 or newer is sufficient; there are no
third-party dependencies or network calls.

```sh
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

The first command checks starter imports, fixture shape, and tokenizer. The
second is the learner acceptance suite and currently fails at the unimplemented
TODO. The CAP-41 comparison is intentionally not scored until that baseline is
present; do not invent either ordered results or scores.

## Explain back

Why is relying on input order insufficient for reproducibility? Why must
deduplication include the source version?

## References

- [Python sorting — key functions, stable sorting, and complex sorts](https://docs.python.org/3/howto/sorting.html)
- [Python unittest — `assertEqual` and `subTest`](https://docs.python.org/3/library/unittest.html)
