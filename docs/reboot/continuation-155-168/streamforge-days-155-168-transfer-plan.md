# Engineering units 155–168 · calendar study days 176–189 · 2027-04-19–2027-05-02 — synthetic temporal-data foundation

Calendar mapping: stable engineering unit IDs 155–168 occupy study days 176–189 (2027-04-19 through 2027-05-02).

**Draft starter scope. No board tickets have been created or updated.** This batch prepares and validates a tiny original synthetic catalog and event log, splits unique events by a fixed UTC cutoff, and emits reproducible local JSON with train-only summaries and separate holdout diagnostics. It is not a recommender, ML model, service, media importer, or deployment.

- Base source: `5dc558eeda441177dc4aac87725d0a18417c5abd`; isolated branch `learning/streamforge-days-155-168`.
- Sprint 13 dates: 2027-04-19 through 2027-05-02.
- Engineering is 165 minutes/day (25 study, 130 implementation/testing, 10 evidence) plus one separate shared 45-minute question-bank block.
- Python interpreter observed before publication: Python 3.12.14. No third-party Python dependency is required.
- The earlier Days 153–154 learner-owned A/B evidence gate has status not met: `evidence/continuation-141-154/day-153.json` records `not_attempted` / `not_run`, tested-source SHA null, 0 artifact hashes, and learner completion `false`; `evidence/continuation-141-154/day-154.json` records `not_attempted` / `not_run`, tested-source SHA null, 0 artifact hashes, and learner completion `false`. The StreamForge scaffold and instructor witnesses cannot satisfy it. Re-evaluate the authoritative records when genuine learner evidence is added; keep progression blocked until both qualify under `manifest.json`.
- No AWS/cloud resource, credential, spending, deployment, external media ingestion, user history, or real learner data.

Each day uses 165 engineering minutes: 25 study, 130 implementation/testing, and 10 evidence, plus one separate shared 45-minute question-bank block.

## Data contract

The checked-in `basic` fixture contains 30 invented text-only catalog rows and 200 synthetic viewing events. It uses invented media/viewer/event IDs; there are no media files or URLs. Manifest `source_kind` is a fixture declaration, not a rights/privacy detector.

Manifest schema version 1 contains exactly the fixture ID, source declarations, filenames and fixed window start/cutoff/end. Its JSON reader rejects duplicate object keys at every nesting level before learner composition. Inputs are local registry children only; each file is limited to 256 KiB, 500 rows, and 4 KiB (4096 UTF-8 bytes) per field. No arbitrary paths or URLs. CSV headers/widths are exact; duplicates, missing or extra columns and malformed rows fail. CSV values remain strings until typed validation. Fixture admission preserves timestamp strings; Day 157 parses UTC values.

Catalog fields are `media_id,title,genre,duration_s,available_from_utc,available_to_utc`. Event fields are `event_id,viewer_id,media_id,occurred_at_utc,watch_seconds`. UTC timestamps accept only `YYYY-MM-DDTHH:MM:SSZ`; availability is `[from,to)`. Duration is an integer 1–7200; watch seconds is an integer from 0 through duration. Allowed genres are `drama`, `comedy`, `documentary`, `animation`, and `science-fiction`. IDs/titles are exact and are not silently trimmed or merged. Duplicate catalog IDs fail even for identical rows.

After all records are validated and joined by exact media ID, duplicate event IDs are resolved globally before partitioning: equal normalized typed payloads collapse once; conflicting payloads reject preparation. Distinct IDs remain distinct. The fixed observation interval is `[start,end)`, with `start < cutoff < end`; train is `[start,cutoff)` and holdout is `[cutoff,end)`. Outside-window records are separately counted. Do not random-split or choose the cutoff from the outcomes. Same viewers can be in both sets; this is temporal, not user-disjoint evaluation. Catalog is reference data, not an as-of-cutoff feature set.

The instructor CLI bundle has `schema_version`, `reference_catalog`, `train`, `holdout`, `outside_window`, `training`, `holdout_diagnostics`, `source_sha256`, and `duplicate_events_collapsed`. `training` contains status, event/viewer totals, sorted per-media event/watch counts, and `content_sha256`. The train-content digest is SHA-256 over canonical UTF-8 JSON containing exactly `schema_version`, fixed `window_start_utc`, fixed `cutoff_utc`, sorted `train` event rows, and the complete training summary without its hash field. Source hashes cover the exact manifest/catalog/event bytes. Holdout fields and source hashes never enter the train-content digest.

The hand-calculable Day 166/167 CLI fixture has train IDs `e-01,e-02`, holdout ID `e-03`, and outside ID `e-04`. Its training summary is 2 events, 2 viewers, media counts `{m1: 1, m2: 1}`, and watch totals `{m1: 10, m2: 20}`. Day 167 checks these values and hashes against an independent test oracle.

Train outputs use only training events and fixed contract/split configuration. Holdout data/coverage and full-source hashes remain separate. A valid holdout mutation can change provenance and holdout diagnostics, but not train content or its hash. Empty train and holdout are valid, with `insufficient_training_data` and `no_holdout` respectively; never invent a score, NaN or zero-denominator ratio. Invalid rows fail the entire preparation, not get silently dropped.

## Assistant-supplied implementation architecture

The assistant specifies the capstone architecture for each implementation day; learners implement and test those decisions. See [day-by-day explanations](assistant-architecture-notes.md). System-design practice occurs only in the separate question-bank sessions on Days 155, 159, 163, and 167. Those sessions use the separate shared 45-minute block; engineering practice remains within the 165-minute allocation. The four questions are Q09, Q15, Q24, and Q34.

## Bounded architecture

`ai/streamforge` is separate from `ai/retrieval/ranking.py` and CAP44. Instructor scaffolding supplies typed models, fixture registry, bounded byte reader, canonical JSON helper, CLI framing, fixtures and subprocess tests. Learner functions are named chronological seams in `contracts.py`; each starts with a named `NotImplementedError`. No database, network client, model, ranking function, vector store, pandas, or third-party dependency is added.

The CLI accepts only a registered fixture name. It writes a single complete JSON document to stdout only after successful preparation; errors go to stderr with nonzero exit and no success document. The Python socket/URL child guard is a bounded API guard, not a claim of OS-wide network isolation. Hashes identify content and do not prove rights, authenticity, quality, privacy compliance, or production readiness.

## Exact documentation sources

No exact Python lesson was found in the preferred course catalog. No Python course or invented lecture is assigned. All daily study blocks use focused official Python 3.12 documentation and a concrete project example within 25 minutes; optional Java-course references are only conceptual bridges and do not teach Python. See `verified-data-foundation-155-168-catalog.json`.

## Verification commands

Default-green supplied infrastructure:

```sh
python3 -m unittest ai.streamforge.tests.test_infrastructure
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Chronological learner acceptance remains red at named TODOs until completed:

```sh
python3 -m unittest ai.streamforge.tests.test_day_contracts.Day155Test
python3 -m unittest ai.streamforge.tests.test_cli.Day167Test
python3 -m ai.streamforge.prepare --fixture basic
```

Full infrastructure/regression and manifest checks are recorded in the local receipt. Temporary witnesses are instructor evidence only, are restored byte-for-byte, and do not confer learner completion.
