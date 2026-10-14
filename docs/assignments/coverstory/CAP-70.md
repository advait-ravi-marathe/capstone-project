# CAP-70 — Stage protected consumer payload envelopes

The saved learner seam is `stage_envelopes(batchId, lines, protectedEnvelopeSink)`. It should stage only encrypted envelope fields bound to an owner, immutable object ID, and revision. It must preserve ciphertext-with-tag, nonce, wrapped-DEK bytes, KEK reference, encryption version, physical line provenance, and a hash of the exact ciphertext bytes. Empty physical lines are skipped without renumbering later input. Unknown/plaintext fields receive safe line/code diagnostics; no raw source line is included in an exception or evidence report.

## Instructor scaffold

- Learner function and typed port contracts: `ai/coverstory/cap_70.py`. `stage_envelopes` remains a TODO.
- Synthetic input and explicit fixed vectors: `fixtures/coverstory/CAP-70/synthetic-v1.json` (SHA-256 `d0a617495aeceda4316ef28ea75045f08a3556a7b41a5831df7ec8821bd48758`).
- Independent reference oracle with fake owner/authentication/immutable-storage ports: `ai/coverstory/tests/test_cap_70_instructor.py`. It verifies ordering, line provenance, exact byte retention, safe rejection, and all-or-nothing batch staging. These fakes are not a product store or cryptographic adapter.
- Existing Java AES-GCM adapter checks: `src/test/java/coverstory/platform/crypto/CAP70CryptoInfrastructureTest.java`. This verifies the synthetic AES-GCM fixed vectors, AAD/tag failure, unavailable-key failure, and distinct nonces from ordinary encryption.
- Learner acceptance contract: `ai/coverstory/tests/test_cap_70.py`. The staging tests are expected to stop at the explicit TODO; the fixture-separation assertion alone does not demonstrate learner implementation.

The vector’s ciphertext bytes are validated against the existing field cipher. Its wrapped-DEK/KEK fields are opaque synthetic fixture bytes: this repository has no envelope-wrapping service, KMS integration, or product staging adapter. No plaintext or consumer data is persisted, and the instructor Python oracle does not claim to authenticate a real wrapped key.

## Run

```sh
python3 -m unittest ai.coverstory.tests.test_cap_70_instructor -v
mvn -Dtest=CAP70CryptoInfrastructureTest test
python3 -m unittest ai.coverstory.tests.test_cap_70 -v
```

The first two commands should pass. The learner command is expected to have four TODO errors and one fixture-only pass. Owner authorization must precede authenticated-envelope validation; missing keys, unsupported versions, invalid AAD/tags, and malformed lines fail closed. No application-revision link is created by this staging exercise.
