# CAP-96 — Separate scaffold health from learner results

This is an offline, local evidence gate for the saved CAP-96 contract. It is not a CI workflow, package publication, deployment, or release approval. A PASS only means the supplied local evidence meets the bounded fixture contract.

## Run

```bash
python3 -m unittest ai.tests.test_coverstory_cap96_infrastructure -v
```

The infrastructure suite is independent of the learner seam and must remain green when the learner TODO is correctly implemented. Its fixtures live under `fixtures/coverstory/CAP-96/reports/`.

The local command accepts an input manifest with report paths:

```json
{
  "command_exit": 0,
  "smoke_report": "fixtures/coverstory/CAP-96/reports/smoke-pass.json",
  "learner_report": "fixtures/coverstory/CAP-96/reports/learner-pass.json",
  "oracle_report": "fixtures/coverstory/CAP-96/reports/oracle-valid.json"
}
```

```bash
python3 scripts/coverstory_local_gate.py /path/to/local-evidence-input.json --output /path/to/local-gate-result.json
```

The gate emits one of `PASS`, `FAIL`, `INCOMPLETE`, or `HARNESS_INVALID` and returns zero only for `PASS`. Reports need stable report IDs and explicit assertion lists. A missing learner report is `INCOMPLETE` even for exit zero. Failed learner assertions are `FAIL` even with healthy smoke. A smoke report that fails or has the permanent TODO 501 sentinel is `HARNESS_INVALID`. A known defect must be rejected by the independent oracle; the oracle report remains marked retained whether the defect is rejected or exposes a harness fault.

## Learner seam

The gate evaluator in `scripts/coverstory_local_gate.py` is instructor-owned local infrastructure. Do not change its fixtures or oracle to make a result green. The learner work is to wire an agreed local test command to write explicit smoke and learner reports; no repository workflow or remote execution is part of this assignment. There is no new build dependency or CI workflow.

## Coverage boundary

The infrastructure tests verify six gate decisions, including all five saved cases and an additional invalid-oracle case. They do not demonstrate an application build, a learner implementation, or publication. Any real result record must capture command, source revision, tool versions, fixture/configuration hashes, and observed outputs; this instructor fixture data is not a run receipt.
