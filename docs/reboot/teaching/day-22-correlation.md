# Day 22 — Correlation without leaking request data

Required study is the owned course “Step 09 - Quick Overview - Importance of Logging with Spring Boot” (9:53). Actuator monitoring (6:54) is optional replacement-only reference. The 25-minute study block is 9:53 course, 2:00 focused reading, 8:00 worked trace, 3:00 prediction, and 2:07 pause buffer.

## Worked run

The runnable instructor reference is `Day22CorrelationReferenceTest`:

```sh
./mvnw -Dtest=Day22CorrelationReferenceTest test
```

It selects a valid incoming toy `X-Trace-Id`, otherwise generates one; the selected ID is placed in a request attribute and response header before the handler runs. For a 400, only that ID is passed to the audit sink afterward. Unsafe `bad\nline` is replaced by `G1`; a 200 is not audited. The actual test checks the same ID in the attribute, header, and captured audit value.

The capstone uses `X-Request-Id`, grammar `[A-Za-z0-9._-]{1,64}`, injected generator, and `RequestAudit.recordClientError(String)`. Do not copy the toy's 16-character limit. Correlation is not authentication; do not record bodies, cookies, authorization headers, exception payloads, or the ID as a metric label.

**Readiness prompt:** Trace a 400 and a 200 through before-chain, handler, and after-chain steps. Explain why validating before echoing protects the boundary. Engineering remains 25/85/10.
