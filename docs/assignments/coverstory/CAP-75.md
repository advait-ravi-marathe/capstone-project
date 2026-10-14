# CAP-75 — Classify provider ingestion failures safely

## Learner seam and source mapping

Implement `classify_provider_failure(run)` in [cap_75.py](../../../ai/coverstory/cap_75.py). The `ProviderFailureRun` typed input and `ProviderFailure` output define the seam. Use the synthetic cases in [synthetic-v1.json](../../../fixtures/coverstory/CAP-75/synthetic-v1.json), learner tests in [test_cap_75.py](../../../ai/coverstory/tests/test_cap_75.py), and independent instructor checks in [test_coverstory_cap75_infrastructure.py](../../../ai/tests/test_coverstory_cap75_infrastructure.py).

Run instructor setup independently:

```sh
python3 -m unittest ai.tests.test_coverstory_cap75_infrastructure
```

Run learner acceptance separately. The four TODO errors are expected until the learner implements the classifier:

```sh
python3 -m unittest ai.coverstory.tests.test_cap_75
```

## Contract

Malformed JSON at batch `b1`, line 2 is `PARSE / MALFORMED_JSON / retryable=false`, with evidence `b1:2`. A SQL timeout known to precede commit is `LOAD / DB_TIMEOUT / retryable=true`; recovery replays or looks up the same batch and provider reference. It must not create another payment. Reuse of a provider event ID with a different payload hash is `IDEMPOTENCY / EVENT_HASH_CONFLICT / retryable=false`.

An unknown exception is `UNKNOWN / UNKNOWN / retryable=false`, requiring reconciliation. Safe evidence may contain batch, line, or event identifiers only. Never return exception text, exception type, raw provider content, or secret-bearing details. The fixture’s synthetic secret exists only to verify output redaction.

## Instructor setup and learner ownership

The instructor oracle uses structured synthetic facts and is independent of the learner classifier. It checks every acceptance case and includes a defective blanket-retry/exception-leak policy as a negative witness. The learner owns classification logic and recovery interpretation. Passing the setup suite validates these fixture and oracle checks only; it does not prove independent learner mastery or any real provider integration.
