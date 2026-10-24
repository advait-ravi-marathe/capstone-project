# StreamForge Days 183–196: a bounded content-profile experiment

**Implementation starter.** This batch compares a transparent train-derived genre-profile heuristic with the existing global-popularity algorithm on the existing synthetic temporal fixture. It adds no learned optimizer, external data, service, or dependency. Publication and scaffold tests do not establish learner mastery.

Current continuation: branch `work/eai-sprint2-onward`, based for this increment on `41946e432b2a61ab4ba1c0fb938da07aff9e5ded`. The separately retained starter provenance is branch `learning/streamforge-days-183-196`, parent receipt `fa45d70868e93da79bb2ff84a8c4a4ca77510924`, and content source `d84c6d42820083c2dc23b4171a351e2932e2dc8d`. Python 3.12 standard library only. The existing global `RankedList` and `BaselineEvaluation` types stay unchanged.

## Clock and prerequisites

Each day uses 165 engineering minutes (35 study, 115 practice/tests, 15 evidence) plus one shared 45-minute interview/new-question/unfinished-review block, 210 minutes total. The shared block is counted once and is separate from the 165 engineering minutes. Days 189 and 196 are recovery days. DSA assignments and factual review clocks remain exclusively in the separate DSA preparation repository.

## Frozen protocol

Protocol `streamforge-cutoff-genre-profile-v1`, K=3, fixed genres animation, comedy, documentary, drama, science-fiction. Each item has one five-coordinate one-hot vector. Positive training signal is the existing set of distinct `(viewer_id, media_id)` pairs with `watch_seconds > 0`. Count each distinct positive media once in its genre, including valid historical items expired at cutoff. Normalize by exact `Fraction` L1 totals. Score candidate one-hot vectors by dot product. Use the original cutoff candidate set for both recommenders.

For a nonzero profile, sort by exact score descending then `media_id` ascending and take at most three. A positive profile with zero candidate overlap keeps `genre_profile` mode and ID ordering. An unseen viewer uses the current global ranking with `global_cold_start`; a seen viewer with zero positive profile uses it with `global_no_positive_profile`. Empty candidates override selection mode with `no_candidates`. Do not remove seen items or rerank by event time.

Use the same positive-target viewer cohort and all distinct positive holdout targets, including future/unreachable items. Precision denominator remains 3, recall denominator is all positive targets, and aggregate metrics are macro means. Report signed profile-minus-global deltas. Empty-state precedence: no_holdout, no_positive_holdout, no_candidates, available. No-candidate positive targets yield exact zero metrics. The 2,500 scored-pair guard rejects before loops; do not truncate. Keep the prior 500-row/256 KiB limits.

This reused holdout is a development comparison, not an untouched final test. Synthetic observed-positive overlap does not prove user preference, unbiased quality, causal lift, fairness, business success, production readiness, or certification readiness. Catalog schedule knowledge at cutoff is a synthetic assumption; no historical catalog snapshot exists.

## Prior evidence gate

The Day 153/154 learner-owned A/B gate remains required before StreamForge implementation. Run `python3 scripts/check_streamforge_169_182_entry_gate.py`; the status is calculated from the authoritative evidence records, not a frozen completion label. Exit 2 means keep implementation paused and recover the earlier prerequisite. The scaffold and its default-green infrastructure tests do not satisfy the gate.

## Architecture and interview practice

[Assistant-supplied architecture notes](assistant-architecture-notes.md) give the implementation decisions and reasons. Learner work is implementation and tests. System-design practice uses only the separate [question bank](../interview/question-bank.md); the four sessions and other shared-block use are in [the schedule](interview-and-review-schedule.md). No second interview block or DSA task is added.

## Chronological seams

The exact signatures, selectors, dependency edges, dates and numeric oracle are in `streamforge-days-183-196-transfer-plan.json`. `contracts/day-NNN.md` provides the human-readable learner unit for each day. Main implementation module is `ai.streamforge.content_contracts`; Day 194 CLI module is `ai.streamforge.content_evaluate`. Daily unit selectors use typed synthetic values and earlier seams only. The actual subprocess selector is in `test_content_cli`, not the pure-contract module.

- 183: `encode_genre`
- 184: `catalog_features`
- 185: `profile_counts`
- 186: `normalize_profiles`
- 187: `score_candidates`
- 188: `rank_for_viewer`
- 189: prior-only recovery for 183–188
- 190: `assemble_predictor_state`
- 191: `evaluate_profiles`
- 192: `compare_against_global`
- 193: `assemble_profile_report`
- 194: real registered-fixture subprocess composition
- 195: supplied adversarial/fresh-process assertions
- 196: prior-only recovery for 183–195

Instructor boundaries supply immutable result types, validation, JSON/hash framing, the scored-pair limit, and harnesses. Real composition must call existing `prepare_evaluation_input` exactly once, reuse existing preparation and baseline seams without rewriting them, and emit only a complete report after success. Strict manifest duplicate-key checks happen before learner composition. Keep prior learner files unchanged and all new learner seams TODO-red after temporary verification witnesses are removed.

## Report and fingerprints

Exact top-level report keys are `schema_version`, `protocol`, `predictor`, `queries`, `evaluations`, `preparation`, and `limits`. Runtime protocol fields are the fixed rendered values plus actual validated manifest timestamps. Predictor state covers protocol, candidates and features, all training viewers and profile state, and the current global support/order; exclude holdout/query IDs and raw source or old train-content hashes. Query records are sorted positive-target viewers, with full-candidate scores and separate state/query fingerprints. Evaluation includes both current global and profile outputs, signed deltas, all eleven cohort counts, all four selection-mode counts, and complete per-viewer target/hit/metric rows. Preparation is a deep copy of the complete adapter lineage. Ratios are reduced `n/d` strings or null only where the frozen empty policy allows.

Hashes are deterministic SHA-256 fingerprints of canonical JSON bytes. New state/query payloads use sorted keys, compact separators, UTF-8, finite values, and a trailing newline. The preexisting train-content hash has no trailing newline; do not change it. Hashes identify the declared bytes/projections, not authenticity or quality. Take full independent snapshots before calls; frozen dataclasses around mutable dictionaries are insufficient.

## Primary-document study

See `verified-content-features-183-196-guidance.json` for exact focused sections, cautions, worked supplements, and catalog audit. Required study is free public documentation inside the 35-minute study allocation. No learner explain-back is required for capstone architecture; the assistant supplies concise what/why context, and the learner implements and tests the assigned seam. No optional course or video is newly assigned. The exact Fraction/L1 profile and local tie/fallback rules are project decisions, not recommendations attributed to the documentation.

This document preserves the historical Days 183–196 slice and its original publication scope. Days 197–270 are now published in the [current assignment inventory](../continuation-197-270-inventory.json); the approved outcome-based extension through Day 732 is indexed in the [current roadmap](../continuation-271-732/README.md). Publication is planning, not learner completion or gate eligibility.

## Verification and evidence

Default-green checks:

```sh
python3 -m unittest ai.streamforge.tests.test_content_infrastructure scripts.test_streamforge_183_196_manifest
python3 -m unittest discover -s scripts -p 'test_*.py'
```

Learner acceptance intentionally remains red at named TODOs until implemented:

```sh
python3 -m unittest ai.streamforge.tests.test_content_contracts ai.streamforge.tests.test_content_adversarial ai.streamforge.tests.test_content_cli
python3 -m ai.streamforge.content_evaluate --fixture basic
```
