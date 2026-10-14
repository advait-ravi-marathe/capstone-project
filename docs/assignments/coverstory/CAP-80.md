# CAP-80 — Replay mock payment confirmation safely

**Status:** Embedded PostgreSQL instructor scaffolding and reference witnesses are runnable. The learner seam remains TODO. This is not provider integration or assignment-readiness evidence.

- Learner seam: `applyConfirmation` in `src/main/java/coverstory/modules/payments/api/CAP80Seam.java`.
- Synthetic input: `fixtures/coverstory/CAP-80/synthetic-v1.json`.
- Test-only transaction schema: `src/test/resources/coverstory/payments/cap_80_schema.sql`.
- Reusable embedded PostgreSQL adapter: `src/test/java/coverstory/modules/payments/CAP80JdbcHarness.java`.
- Learner contract: `src/test/java/coverstory/modules/payments/CAP80AcceptanceTest.java`.
- Independent instructor transaction oracle: `src/test/java/coverstory/modules/payments/CAP80InstructorInfrastructureTest.java`.

The saved card planned `src/main/java/capstone/coverstory/payments/ReplayMockPaymentConfirmationSafely.java`. This checkout uses `coverstory.modules.payments.api` for the learner seam. Payment recording and the Application onboarding operation meet at one transaction callback; no application code reaches into either module's repository. The JDBC adapter is test-only and stands in for the public Payments transaction port plus the Application public operation.

The bounded fixture uses synthetic owners and `quote-health-A-v1` at 100000 INR paise. It covers all five saved cases: sequence 3 applies from checkpoint 2; replay returns `ALREADY_APPLIED` without another effect or onboarding transition; sequence 4 returns `GAP` without writes; a failure after the mock effect and Application state/timeline writes but before the checkpoint update rolls the whole PostgreSQL transaction back and retry applies once; and principal A's attempt to process owner B's feed returns `FORBIDDEN` before opening a transaction or reading B's checkpoint.

All payment effects are rows in `payments.mock_capture_effect`; there is no provider client or external capture. The instructor oracle is separately implemented in test code. Positive witnesses execute against embedded PostgreSQL; defect witnesses reject premature checkpoint advancement and a duplicate capture. A local PostgreSQL rollback proves the test unit of work only; it makes no deployment or external-provider claim.

```sh
mvn -Dtest=CAP80InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP80AcceptanceTest test
```

The first command should pass. The second currently reaches the learner `UnsupportedOperationException` TODO in all five cases. Instructor setup success does not establish learner mastery or rehearsal time.
