# CAP-72 — Atomic provider batch observations and audit

**Saved contract:** Day 031 · 13 Nov 2026 · 150 minutes maximum. The task remains learner-owned; the starter function intentionally raises its explicit TODO.

Implement `load_provider_batch(batch_id, validated_rows, unit_of_work) -> LoadReport` in [`ai/coverstory/cap_72.py`](../../../ai/coverstory/cap_72.py). Its typed ports model the owning Payments module: `manifests`, `provider_events`, `transaction_audit`, and read-only `payment_effects`. The supplied transaction must encompass every manifest, observation, and audit write. Do not reach into another module's repository.

## Source and inputs

The saved primary contract and exact planned repository paths are in [Day 031](saved-days/day-031-cap-72.md) and the [curriculum index](curriculum-index.json). The expected cases are transcribed there as CAP-72-CS-01 through CS-05. The synthetic batch in [`fixtures/coverstory/CAP-72/synthetic-v1.json`](../../../fixtures/coverstory/CAP-72/synthetic-v1.json) contains one `FAILED` and one `UNKNOWN` observation, plus a later success reconciliation for the same operation. Its event hashes are SHA-256 over each row's canonical JSON with `payload_hash` omitted. It contains no real provider payload, credential, or consumer data.

## Required behavior

- Persist the manifest, validated provider observations, and ordered append-only transaction audit events in one unit of work. An injected failure after any write leaves none of those writes committed.
- Exact batch replay returns the original receipt without duplicate observations or audits. A changed batch body returns `BATCH_CONFLICT`; reuse of a provider event identity with changed validated content returns `EVENT_HASH_CONFLICT` and leaves existing history intact.
- Keep `UNKNOWN` as an observation and retain its safe provider reference for reconciliation. A later success appends a new observation for the same operation; it does not overwrite the earlier uncertainty.
- Audit rows describe attempt/effect facts; they do not post money, transition an application, or unlock onboarding. Read monetary totals from the owning Payments effect record, which is unique per operation.
- Use only server-validated values supplied to the seam. Never persist raw provider payloads, credentials, or consumer form values.

## Run the instructor setup and learner contract separately

```bash
python3 -m unittest ai.tests.test_coverstory_cap72_infrastructure
python3 -m unittest ai.coverstory.tests.test_cap_72
```

The instructor suite uses a test-only transactional store and reference oracle. It covers ordered positive observations, exact replay, both conflict types, rollback after each of five writes in this fixture, and UNKNOWN reconciliation. Defect witnesses show that reversed observations, replacing UNKNOWN with FAILED, and adding money effects from audit counts are rejected. This verifies harness behavior; it does not implement or pass the learner function.

The acceptance suite invokes the learner seam against a persistence-only in-memory adapter. While the explicit TODO remains, its errors at `load_provider_batch` are expected. Do not report the assignment as complete or infer mastery from the instructor oracle passing.
