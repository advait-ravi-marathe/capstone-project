# CAP-92 — Dry-run source retention boundaries

**Status:** instructor scaffold supplied; learner classifier remains TODO. This is a synthetic teaching exercise, not a legal retention rule or production deletion job.

## Practical repository work

Implement `CAP92Seam.classifyRetention` in `src/main/java/coverstory/modules/application/api/CAP92Seam.java`. Use the supplied clock and synthetic fixture in `fixtures/coverstory/CAP-92/synthetic-v1.json`. The intended command is:

```sh
mvn -Dtest=CAP92InstructorInfrastructureTest test
mvn -Dtest=CAP92AcceptanceTest -Dgroups=learnerAcceptance test
```

The learner suite is expected to fail at the explicit `UnsupportedOperationException` until the TODO is completed. Instructor oracle tests must pass before and after that work. Run both separately so scaffold health is not confused with learner completion.

## Contract boundaries

- Apply owner scope before classifying records; the report for A must not disclose B's record.
- The fixture's raw-body threshold is 30 days and audit-reference threshold is 90 days, inclusive. These values are synthetic only.
- Future and null timestamps are `INVALID_TIME`, not retained based on negative age.
- An older record can only be marked `CANDIDATE_DELETE`. The seam has no delete port and reports zero delete calls.
- Audit output can include a non-content reference but never body content. Accepted purchase/payment history is outside this exercise.

## Evidence and discussion

Use the fixed clock for exact boundary cases. Explain why a dry-run and explicit owner scope matter, and identify how malformed timestamps should be reviewed. Do not claim deletion, a real retention schedule, legal compliance, or learner completion based on the passing instructor oracle.
