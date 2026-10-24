# Days 211–224: bounded synthetic policy explanations

Status: reviewable starter curriculum; no learner completion or board publication is claimed. Dates are 22 May–4 June 2027. Main and prior Days 1–210 remain preserved.

## Architecture and acceptance

The assistant supplies the architecture: ingest only a bounded synthetic bundle; require a real, learner-owned Day 210 boundary review before attempting ingestion; validate exact schema and immutable source/version identity; filter approved owner-scoped rows before ranking; return exact citations or abstain. Learner implementation remains a TODO in `ai/grounded/continuation_211.py`; this package is not a completed retrieval implementation. No provider or cloud service is called.

Run `python3 scripts/check_continuation_211_entry_gate.py` to inspect current evidence. At this checkpoint it reports `not_met`: the Day 210 learner-owned review is absent, and completion_awarded is false. Recovery means use the first unmet selector from the manifest, preserving the existing recovery queue. AI practice slices remain offline fixed-fixture/evaluator work. Gate status can change only from actual evidence; scaffold or CI does not satisfy it. The learner record uses a 40-character lowercase Git commit SHA for `source_sha` and a 64-character lowercase SHA-256 digest for `artifact_sha256`; the checker reports eligibility only and awards no completion.

The acceptance tests in `ai/grounded/learner_acceptance/test_continuation_211.py` are intentionally red until the learner implements the TODOs. They check schema rejection, draft and owner filtering, stable top-k, citation/version grounding, abstention, independent metrics, hostile source text, canonical bytes and fresh-process output. These tests have not been represented as passing.

<a id="entry-gate-and-recovery"></a>
## Entry gate and recovery

Use the first unmet selector in `days-211-224-transfer-plan.json` `recovery_selectors`. Keep evidence statuses truthful and stop new ingestion while the entry gate is not met.
