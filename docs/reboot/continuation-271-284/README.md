# Engineering units 271–284 · calendar study days 292–305 · 2027-08-13–2027-08-26 — runtime and SQL measurement foundations

Calendar mapping: stable engineering unit IDs 271–284 occupy study days 292–305 (2027-08-13 through 2027-08-26).

## Entry and recovery

At the start, record actual outputs from `python3 scripts/check_continuation_239_entry_gate.py`, `python3 scripts/check_continuation_253_entry_gate.py`, and `python3 scripts/check_streamforge_169_182_entry_gate.py`. Missing/invalid evidence remains dynamically unmet, not a permanent failure. The offline measurement lab is independent of StreamForge eligibility and does not alter gated learner code. If an authoritative learner blocker exists, use each day's 33-minute reserve for its next bounded recovery action; otherwise use those minutes for the day's named integration, observation or reassessment. Do not invent completion evidence.

## Daily allocation

Each day is **165 engineering minutes** (35 study, 115 practice/tests, 15 evidence) plus **one shared 45-minute** question-bank/review session. The 33-minute reserve is included in practice/tests. This sprint is 2,310 engineering minutes (38.5 h) and 630 shared minutes (10.5 h); the reserve is 462 minutes (7.7 h), 20% of engineering capacity.

The assistant supplies the architecture: immutable synthetic workload → loopback-only disposable PostgreSQL plan/result capture → independent analyzer → evidence report. The learner implements the analyzer and assigned tests. This is offline, deterministic training; timing is observation, never a CI performance threshold. No cloud/provider call, secret, paid service, customer data or deployment. SQL experiments compare a skewed distribution before/after `ANALYZE` and alternative index layouts while preserving exact answer rows. JFR runs a bounded 500-million-iteration CPU workload with a custom phase event; inspect its event fields and summary, while sampled execution-event counts remain variable observations.

Canonical assignments are in [manifest.json](manifest.json), with a no-board-write [Story export](scrum-owner-manifest.json), source records in [verified-sources.json](verified-sources.json), assistant rationale in [architecture notes](assistant-architecture-notes.md), and rendered contracts under [`contracts/`](contracts/). The local PostgreSQL capture script is `scripts/capture_runtime_sql_fixture.sh`; its database binds only to loopback and is removed at exit. It defaults to the ignored `target/runtime-sql-lab/observation.json`, leaving the checked-in `labs/runtime_sql/fixtures/observation.json` immutable. The latter is one captured reference whose stored 3 hit/2 read counters are asserted only for those bytes. Fresh-run checks instead compare reported counters with that run's own plan and allow cache-dependent values. Supported variants are `skew`, `without-index`, and `reverse-index`. The learner seam is `labs/runtime_sql/learner_analysis.py`; acceptance tests are in `labs/runtime_sql/tests/test_acceptance.py`; the separate instructor reference is `labs/runtime_sql/reference_analysis.py`.

Expected ordered row identities/count and plan arithmetic come from independent fixture facts. Counterfactual and malformed-input negative controls exercise the same acceptance entry point, including mutations of owner, policy, revision, order, missing rows and extra rows. Timing values are intentionally not asserted. PostgreSQL cautions that estimates/costs depend on data and platform; tests check row correctness and arithmetic, not one node type or a performance promise.

## Shared 45-minute block

System-design practice uses only the separate [interview question bank](../interview/question-bank.md). Each day's prompt is distinct; it never becomes a capstone architecture assignment. The assistant supplies capstone architecture and explains why; the learner implements and tests.

## Validation

Run `python3 scripts/generate_continuation_271_284.py --check`, `python3 -m unittest scripts.test_continuation_271_284`, and `python3 -m unittest scripts.test_jfr_runtime_sql`. A green infrastructure/reference harness is not learner completion, gate eligibility, AWS readiness, professional ownership or a production claim.
