# CAP-125 instructor scaffold: authorize each lifecycle evidence read

The saved assignment is [Day 093](saved-days/day-093-cap-125.md). Its learner-owned business decision remains the `TODO(CAP-125)` in `LifecycleToolAuthorization.authorize`. The instructor supplied compile-time wiring and a synthetic, deterministic owning-module port; neither implements the learner's authorization decision.

## What is prepared

- Learner seam: `src/main/java/capstone/coverstory/support/internal/LifecycleToolAuthorization.java`.
- Mechanical read-only tool wrapper: `invokeReadOnlyTool`; a `FactsPort` cannot perform cancellation, refund, payment, or other writes.
- Independent expected-outcome oracle and three fixed learner cases: `src/test/java/capstone/coverstory/support/CAP125ReferenceOracle.java` and `LifecycleToolAuthorizationTest.java`.
- Instructor-only fixture and adapter checks: `CAP125InstructorInfrastructureTest.java`.
- Frozen synthetic inputs: `docs/coverstory/fixtures/cap-125.json`.
- Result and architecture/interview templates: `docs/evidence/coverstory/CAP-125/` and `docs/architecture/coverstory/CAP-125-delta.md`.

The wrapper rechecks the owner/assignment relation after the learner decision and before serializing a minimal cancellation-state result. A production adapter must bind those facts to the owning Application and Support APIs and resolve current support assignment on each invocation. UUIDs, model claims, conversation state, and prior approvals are not authority.

## Commands

Instructor setup and oracle, independent of learner implementation:

```sh
./mvnw -Dtest=CAP125InstructorInfrastructureTest test
```

Learner cases, deliberately separate and expected to stop at the TODO until implemented:

```sh
./mvnw -Plearner-tests -Dtest=LifecycleToolAuthorizationTest test
```

The oracle proves the expected synthetic outcomes and adapter fixture are executable; it does not prove the learner seam or production integration. Record observed command results and environment in the result file.

## Explicit runtime boundary

This repository has no public Support assignment read API or owning Application cancellation-state API ready for binding. The fake port makes the harness executable without adding cross-module repository access. PostgreSQL row security is not a replacement for per-invocation application authorization; production must also avoid private serialization before authorization. No real cancellation/refund/payment command is supplied. CAP-124 remains a named prerequisite and its readiness is not inferred from these tests.
