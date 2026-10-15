# CAP-48 — UTC daily catalog evidence buckets

The saved contract names `src/main/resources/coverstory/catalog/cap_48.sql`; that path is used. Its daily date spine and evidence remain test-only synthetic data.

## Instructor-owned setup

- Schema/date spine and deterministic synthetic records: `src/test/resources/coverstory/catalog/cap_48_schema.sql` and `cap_48_fixtures.sql`.
- Learner query starts as a valid typed empty-result SQL TODO at `src/main/resources/coverstory/catalog/cap_48.sql`.
- Embedded PostgreSQL acceptance: `src/test/java/coverstory/modules/catalog/CAP48SqlAcceptanceTest.java`.
- Actual database/fixture smoke: `src/test/java/coverstory/modules/catalog/CAP48InstructorInfrastructureTest.java`.
- Run setup smoke: `mvn -Dtest=CAP48InstructorInfrastructureTest test`.
- Run learner acceptance separately: `mvn -Plearner-tests -Dtest=CAP48SqlAcceptanceTest test`.

The cases require UTC date buckets independent of session timezone, date-spine rows for empty days, nullable amount counting without pricing, and editor isolation. The independent instructor oracle now checks actual PostgreSQL 14 rows under UTC and Asia/Kolkata and rejects observed-only grouping, session-local date casting, and wrong-editor aggregation. Integer paise are not payable quotes. It does not prove learner behavior or timebox fit; no timed rehearsal has been performed.
