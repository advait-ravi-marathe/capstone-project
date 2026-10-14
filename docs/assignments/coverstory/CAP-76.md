# CAP-76 — Bound safe reconciliation reads

**Saved day:** 036 · 18 November 2026 · **Primary ID:** CAP-76 · **Maximum allocation:** 150 minutes

## Purpose

Retain an attempt and time budget for idempotent mock-provider status GETs only. The supplied seam does not send requests or retry payment creation/capture. A timeout after a possibly committed charge remains `UNKNOWN_REQUIRES_RECONCILIATION`; resolve it through the same operation’s status read before taking any further payment action.

## Files

- Learner seam: `src/main/java/coverstory/modules/payments/api/CAP76Seam.java`
- Learner acceptance: `src/test/java/coverstory/modules/payments/CAP76AcceptanceTest.java`
- Instructor self-check: `src/test/java/coverstory/modules/payments/CAP76InstructorInfrastructureTest.java`
- Synthetic contract: `fixtures/coverstory/CAP-76/synthetic-v1.json`

The learner owns `RetryDecision next(int attemptNumber, int httpStatus, Duration elapsed, Duration parsedRetryAfter)`. Attempts are numbered 1–3. The deadline is 1,000 ms, and elapsed time plus delay must be strictly less than the deadline. Backoff for attempts one and two is 100 ms and 200 ms. A valid Retry-After is honored as a minimum delay.

## Required behavior

1. Return known 2xx success before applying deadline or attempt exhaustion, retaining the response even if it arrived after the deadline.
2. Treat 408, 429, and 5xx responses as transient. Other statuses such as 403 stop permanently.
3. Stop when attempts are exhausted or the selected backoff cannot fit strictly inside the time budget.
4. For a long Retry-After, defer the status read instead of polling earlier than requested.
5. Keep possible create/capture timeout uncertainty outside this automatic policy. Never create another charge to resolve an unknown outcome.

## Checks

Run the independent instructor witnesses:

```sh
mvn -q -Dtest=CAP76InstructorInfrastructureTest test
```

Run the learner contract separately. It is expected to reach the explicit TODO until implemented:

```sh
mvn -q -Plearner-tests -Dtest=CAP76AcceptanceTest test
```

A passing reference test is setup evidence only. Record learner acceptance, implementation status, and any timebox rehearsal separately.

## Saved acceptance mapping

| Case | Input | Expected result |
|---|---|---|
| CAP-76-CS-01 | Attempt 1, 503, elapsed 0, no header | `RETRY_AFTER` 100 ms |
| CAP-76-CS-02 | Attempt 2, 429, elapsed 100 ms, no header | `RETRY_AFTER` 200 ms |
| CAP-76-CS-03 | Attempt 3 transient, or attempt 2 at elapsed 900 ms with 200 ms delay | `STOP_EXHAUSTED` |
| CAP-76-CS-04 | Attempt 1, 403 | `STOP_PERMANENT` |
| CAP-76-CS-05 | 2xx known after deadline | `STOP_SUCCESS`; response retained |
| CAP-76-CS-06 | Retry-After 31 seconds or possible capture timeout | Defer status read; possible money effect remains `UNKNOWN_REQUIRES_RECONCILIATION`; no new charge |

## Interpretation

For the status GET policy, a retry requires both an available attempt and enough remaining time. A known success response is authoritative even when late. Acknowledging or retrying a status read cannot make an uncertain capture safe to repeat; that uncertainty requires reconciliation by its existing operation reference.
