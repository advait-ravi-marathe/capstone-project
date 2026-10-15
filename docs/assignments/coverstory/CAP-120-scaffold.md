# CAP-120 instructor scaffold: clause offsets, recall, and context cost

**Readiness:** source-backed fixture and independent oracle authored; execution status is recorded in `docs/evidence/coverstory/CAP-120/result.json`. Learner implementation remains TODO. No timebox rehearsal or learner completion is claimed.

## Run commands

```sh
./mvnw -Dtest=CAP120InstructorInfrastructureTest test
./mvnw -Plearner-tests -Dtest=ClauseChunkerTest test
```

The first command verifies only the instructor's independent arithmetic and frozen fixture. The second command exercises `ClauseChunker` and is expected to stop at its explicit TODO until the learner implements it. A passing instructor oracle is not learner evidence.

## Fixed experiment

Fixture `docs/coverstory/fixtures/cap-120.json` has two synthetic UTF-8 documents, pinned by per-source SHA-256 plus a fixture digest. Gold spans are half-open byte ranges. One crosses the `Coverage` heading into the following body, so converting Unicode text to characters or token indexes cannot masquerade as a stable citation.

The only tokenizer is whitespace splitting on Unicode text; each non-whitespace sequence counts as one token. Context cost is the sum of token counts across every emitted window, including repeated overlap tokens. Compare `compact` (5 tokens, overlap 2) with `wide` (14, overlap 4) against a 44-token combined context budget. Expected recall is the fraction of two whole gold spans fully contained in at least one window. No model, embeddings, index, provider, private document, or legal interpretation is involved.

The oracle rejects negative/empty/reversed/out-of-source spans, unordered or overlapping spans, UTF-8 continuation-byte endpoints, and overlap at least the configured size. The actual seam must independently implement those checks. Its tests deliberately preserve the saved logic as TODO.

## Evidence limits

Instructor checks do not establish runtime Support API wiring, persistence, a production tokenizer, model behavior, insurance/legal correctness, or learner mastery. Record any observed learner test output separately; do not replace `NOT_RUN` with a planned result.
