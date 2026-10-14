# CAP-99 · Local readiness evidence manifest

**Status:** instructor harness partially verified; learner acceptance remains intentionally unimplemented. This is a bounded offline teaching slice, not a production launch decision.

## Scope and interface

Implement `CAP99Seam.evaluateReadiness(List<GateEvidence>)` in [CAP99Seam.java](../../../src/main/java/coverstory/application/CAP99Seam.java). It evaluates exactly six gates: artifact, redacted config, migration compatibility and restore, ownership, payment recovery, and grounded assistance. `READY_FOR_REVIEW` means only that complete, real evidence has been assembled for human local review. It is not learner readiness, release approval, insurer integration, deployment authorization, or proof that the product gates passed.

A `VERIFIED` receipt must include an observed test output containing `PASS`, exact command, 40 character source commit, and 64 character fixture hash. A planned path or screenshot by itself remains `UNVERIFIED`. Missing migration evidence is `BLOCKED` and names the CAP-98 prerequisite. Any ownership or cross-owner failure blocks regardless of the other five gates. Do not average gate statuses.

The learner owns the evaluator and focused assertions. The implementation is an explicit TODO. No complete solution is provided. Synthetic contracts are in [synthetic-v1.json](../../../fixtures/coverstory/CAP-99/synthetic-v1.json); they are not real product evidence.

## Run and interpret the harness

Instructor oracle, independent of learner code:

```sh
mvn -q -Dtest=CAP99InstructorInfrastructureTest test
```

Learner contract (expected to error at the TODO until implemented):

```sh
mvn -q -Dtest=CAP99AcceptanceTest test
```

The instructor oracle checks all-six review-only outcome, missing CAP-98 migration blocker, five-green plus cross-owner failure, screenshot/path evidence quality, and blocker precedence. Passing this oracle proves only that the synthetic readiness contract is runnable. Actual product gate evidence stays `UNVERIFIED` until separately collected and reviewed.

## Evidence boundary

See [result.json](../../evidence/coverstory/CAP-99/result.json), [fixtures.json](../../evidence/coverstory/CAP-99/fixtures.json), and [harness-self-check.json](../../evidence/coverstory/CAP-99/harness-self-check.json). The recorded Maven run validates only the instructor oracle. The learner suite currently reports four expected errors at the explicit TODO. No deployment, release, provider call, real consumer data, or actual readiness gate was exercised.
