# CAP-80 — Replay mock payment confirmation safely

**Status:** A real Spring/JDBC runtime adapter now exists behind optional local DB configuration; the saved learner seam remains TODO. PostgreSQL runtime checks prove owner scoping, monotonic feed sequencing, atomic mock capture plus Application-owned onboarding transition, rollback, and retry using an independent instructor reference action. The default command invokes the actual TODO and returns 501 before writes. CAP-80 remains PARTIAL; this is synthetic-only infrastructure, not payment-provider integration or learner completion.

- Learner seam: `applyConfirmation` in `src/main/java/coverstory/modules/payments/api/CAP80Seam.java`.
- Synthetic input: `fixtures/coverstory/CAP-80/synthetic-v1.json`.
- Runtime schema: `src/main/resources/db/schema/constraints.sql`; runtime JDBC adapter: `src/main/java/coverstory/modules/payments/JdbcMockConfirmationFeedAdapter.java`.
- Application public transition port: `src/main/java/coverstory/modules/application/api/OnboardingPaymentTransitionPort.java`.
- Runtime and isolated harnesses: `src/test/java/coverstory/modules/payments/CAP80ReplayRuntimePathIntegrationTest.java` and `CAP80JdbcHarness.java`.
- Learner contract: `src/test/java/coverstory/modules/payments/CAP80AcceptanceTest.java`.
- Independent instructor transaction oracle: `src/test/java/coverstory/modules/payments/CAP80InstructorInfrastructureTest.java`.

The saved card planned `src/main/java/capstone/coverstory/payments/ReplayMockPaymentConfirmationSafely.java`. This checkout uses `coverstory.modules.payments.api` for the learner seam. Payment recording and the Application onboarding operation meet inside one Spring transaction through a public Application port; no Payments code reaches into the Application repository. An injectable instructor action is only used by rollback/recovery tests; application wiring uses the untouched learner seam.

The bounded fixture uses synthetic owners and `quote-health-A-v1` at 100000 INR paise. The independent test-only reference covers the five transaction cases: sequence 3 applies from checkpoint 2; replay returns `ALREADY_APPLIED`; sequence 4 returns `GAP`; an injected failure rolls back; and foreign-feed access is denied before transaction/read. These are SQL/reference witnesses, not an authoritative command runtime or a supplied Application unlock port.

All payment effects are rows in `payments.mock_capture_effect`; there is no provider client or external capture. The instructor oracle is separately implemented in test code. Positive witnesses execute against embedded PostgreSQL; defect witnesses reject premature checkpoint advancement and a duplicate capture. A local PostgreSQL rollback proves the test unit of work only; it makes no deployment or external-provider claim.

```sh
mvn -Dtest=CAP80InstructorInfrastructureTest,CAP80ReplayRuntimePathIntegrationTest test
mvn -Plearner-tests -Dtest=CAP80AcceptanceTest test
```

The first command should pass. The second currently reaches the learner `UnsupportedOperationException` TODO in all five cases. Instructor setup success does not establish learner mastery or rehearsal time.
