# CAP-68 — Expose commit-before-response uncertainty

The saved acceptance contract demonstrates that an atomic shortlist/domain change, business audit event, and journey step event can be committed before an HTTP response is lost. Immediately after that loss, the client state is `UNKNOWN`; it must recover the original operation with the same idempotency key or an owner-scoped read. A missing optional publication is a separate gap and does not erase the SQL/audit receipt.

## Instructor harness

- Learner seam: `src/main/java/capstone/coverstory/shortlist/api/CAP68Seam.java`. The orchestration logic remains TODO.
- Synthetic inputs: `fixtures/coverstory/CAP-68/synthetic-v1.json` (SHA-256 `4b0784ad3cc3bb4ef7a7fe71c4306dd1108b0c879aa998973e0af873c2537f39`).
- Disposable schema: `src/test/resources/coverstory/CAP-68/schema.sql`.
- Independent PostgreSQL oracle: `src/test/java/capstone/coverstory/shortlist/CAP68InstructorInfrastructureTest.java`.
- Learner-owned acceptance suite: `src/test/java/capstone/coverstory/shortlist/CAP68Test.java` (four tagged cases; expected to fail at the preserved TODO).

The instructor oracle has its own test-only reference transaction. It is not the CoverStory `Shortlist` module implementation, an authenticated endpoint, a response-recovery service, a durable notification publisher, or evidence of live product behavior. The fake publication check intentionally leaves notification undelivered while the SQL and audit rows remain committed. No payment or policy effects run.

## Run

```sh
mvn -Dtest=CAP68InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP68Test test
```

The first command should pass independently. The learner command is expected to stop at its unimplemented seam until the learner implements recovery and gap reporting. Keep DB durability evidence separate from HTTP response delivery and from optional notification delivery.
