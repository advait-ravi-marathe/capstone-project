# Day 8 — Make PostgreSQL enforce a composite relationship

**Required course background:** in28Minutes Official, *Master Spring Boot 3 & Spring Framework 6 with Java*, section “Getting Started with JPA and Hibernate with Spring and Spring Boot”: “Step 03 - Launching up H2 Console and Creating Course Table in H2” (7:24; item 35019878). It introduces a table, not the PostgreSQL relationship or constraint failures below. “Step 04 - Getting Started with Spring JDBC” (3:58; item 35019874) is optional later, not stacked today. Catalog title verified; playback not verified.

Study budget: 7:24 course + 13:36 worked PostgreSQL example + 4:00 readiness = 25 minutes. Use the repository's disposable embedded PostgreSQL fixture; do not switch to H2 or alter the learner migration to make this reference pass.

## Worked schema

The instructor reference creates its own teaching-only tables:

```sql
CREATE TABLE teaching_revisions (
  team_id TEXT NOT NULL,
  revision_id TEXT NOT NULL,
  PRIMARY KEY (team_id, revision_id)
);
CREATE TABLE teaching_publications (
  team_id TEXT NOT NULL,
  revision_id TEXT NOT NULL,
  position INTEGER NOT NULL,
  PRIMARY KEY (team_id, revision_id),
  FOREIGN KEY (team_id, revision_id)
    REFERENCES teaching_revisions(team_id, revision_id)
);
```

`PRIMARY KEY(team_id, revision_id)` identifies one revision inside one team. The foreign key checks that exact pair. `NOT NULL` requires each named field. The teaching test seeds red/r1 and blue/r2, then uses a new connection/statement for each case so a constraint failure cannot poison a later case.

| Insert case | Result | Reason |
|---|---|---|
| publication(red,r1,0) | succeeds | The exact parent pair exists. |
| publication(blue,r1,0) | SQLSTATE `23503` | The team and revision exist separately, but the pair blue/r1 does not. |
| publication(red,missing,1) | `23503` | No parent pair exists. |
| repeat publication(red,r1,2) | `23505` | The pair is the row identity; a new position does not make a second row. |
| null revision ID or null position | `23502` | Required fields cannot be null. |
| publication(red,r2,1), after adding red/r2 | succeeds | A team may have multiple distinct revisions. |

Run the complete local PostgreSQL reference:

```sh
./mvnw -Dtest=Day08CompositeKeyReferenceTest test
```

The test owns and closes its embedded server. It does not apply `V001__homesecure_quotes.sql` or supply the solution to that learner migration.

## Practice and transfer

The 85-minute practice allocation remains: up to 15 minutes run and inspect the teaching matrix, 10 minutes predict/verify the green/r1 versus green/r2 variation, 45 minutes on the existing migration constraints, and 15 minutes for the original selector and diagnosis. Evidence remains 10 minutes. If the example takes longer, shrink or carry the implementation; do not add time.

**Readiness question:** Why does the reference foreign key compare both columns? Which invalid pair would pass if it checked only `revision_id`? Then use the actual `journeys`, `quote_versions`, and `selections` contract without copying the toy names.

Existing learner selector: `./mvnw -Plearner-tests -Dtest=Day08ContractTest test`. Instructor reference green and learner migration TODO red are distinct results.

**Primary source:** PostgreSQL [constraints](https://www.postgresql.org/docs/current/ddl-constraints.html), sections “Not-Null Constraints,” “Primary Keys,” and “Foreign Keys.”
