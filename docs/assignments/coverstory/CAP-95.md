# CAP-95 — Bound optional assistance requests

**Saved day:** 058 · 10 December 2026 · **Maximum allocation:** 150 minutes

Implement `requestAssistance` in `src/main/java/coverstory/application/BoundOptionalAssistanceRequests.java`.

The port models a deterministic offline request. Pass exactly one request, the configured output cap and a cancellation token. Input over the limit must return `LIMIT_EXCEEDED` before calling the port. Invalid or missing limits must return `INVALID_CONFIG` before any call. Accept only a grounded, nonempty response with output token usage at or below the configured cap and completion before the deadline. At or after the deadline, cancel the single attempt and return `TIMEOUT`; do not retry. Always return the same `ManualState` value passed to the seam. This policy does not mutate form data, drafts, consent, underwriting or application state, and does not make a real model call.

## Files

- Learner seam: `src/main/java/coverstory/application/BoundOptionalAssistanceRequests.java`
- Learner contract: `src/test/java/coverstory/application/CAP95AcceptanceTest.java`
- Independent instructor oracle: `src/test/java/coverstory/application/CAP95InstructorInfrastructureTest.java`
- Synthetic contract fixture: `fixtures/coverstory/CAP-95/synthetic-v1.json`
- Evidence: `docs/evidence/coverstory/CAP-95/`

## Run checks

Instructor setup and oracle checks:

```sh
mvn -Dtest=CAP95InstructorInfrastructureTest test
```

Learner contract, separately (expected to fail at the explicit TODO until implemented):

```sh
mvn -Plearner-tests -Dtest=CAP95AcceptanceTest test
```

The oracle is independent of the learner method. Its passing checks validate only fixture structure and the contract reference examples; they do not establish runtime provider behavior, production latency, user consent, application correctness or learner mastery. The fake port and clock have no network or model SDK integration.

## Acceptance map

| Case | Expected behavior |
|---|---|
| CAP-95-CS-01 | 2,000 tokens, response at 1,499 ms: one call with output cap 256; accept grounded draft |
| CAP-95-CS-02 | 2,001 tokens: `LIMIT_EXCEEDED`; zero port calls |
| CAP-95-CS-03 | Completion at 1,500 ms: `TIMEOUT`, one cancelled attempt and no retry |
| CAP-95-CS-04 | Null limits or nonpositive deadline: `INVALID_CONFIG` before call |
| CAP-95-CS-05 | Timeout during form use preserves manual value, draft identifier and consent |

The configured output cap is sent to the port and independently checked against reported output usage. Token accounting here is a local teaching contract, not a billing guarantee. The cancellation signal represents the teaching adapter's termination request; it cannot prove that an external provider stopped computation.
