# CAP-42 — Catalog evidence control total

The saved contract names `src/main/resources/coverstory/catalog/cap_42.sql`; that path is used. The surrounding legacy CAP42 files under `assignments/cap42` are a separate historical signed-event exercise and are not repurposed.

## Instructor-owned setup

- Schema and deterministic synthetic records: `src/test/resources/coverstory/catalog/cap_42_schema.sql` and `cap_42_fixtures.sql`.
- Learner query starts as a parameterized, shape-only SQL TODO at `src/main/resources/coverstory/catalog/cap_42.sql`.
- Embedded PostgreSQL acceptance: `src/test/java/coverstory/modules/catalog/CAP42SqlAcceptanceTest.java`.
- Actual PostgreSQL setup check: `src/test/java/coverstory/modules/catalog/CAP42InstructorInfrastructureTest.java`.
- Run setup check: `mvn -Dtest=CAP42InstructorInfrastructureTest test`.
- Run learner acceptance separately: `mvn -Plearner-tests -Dtest=CAP42SqlAcceptanceTest test`.

The cases cover exact editor ownership, PUBLISHED filtering, half-open UTC bounds, empty aggregate output, exact bigint totals beyond 32-bit range, and currency rejection. Amounts are source evidence totals, never customer premiums. Setup smoke establishes that PostgreSQL and fixture/schema boundaries work; it does not validate the learner query or timebox. No timed rehearsal has been performed.
