# Day 59 — Read migration history safely

**Study: 25 minutes.** Redgate “Versioned migrations” and “Naming”; “Flyway Target Setting” → “Migrate”; Spring Boot “Execute Flyway Database Migrations on Startup”. No exact course lesson was verified, so no unrelated video is required.

## Worked example

The independent library fixture in `src/test/resources/teaching/library-migrations/` creates a shelf and inserts a handbook in two versioned scripts. `ShelfMigrationReferenceTest` applies target 2, inserts a reader-created sentinel, runs migrate again, and compares complete ordered rows and schema-history versions/checksums. Expected first migration count: 2; repeat count: 0; sentinel and history unchanged. A startup-reset mutant deletes the sentinel while keeping the table: the full-row comparison catches it.

Run `./mvnw -Plocal-app -Dtest=ShelfMigrationReferenceTest test`. This reference is separate from the learner migration test and uses a fresh embedded PostgreSQL database.

## Transfer

On a newly created disposable database, the restored native selector targets immutable V001–V003: first run 3, repeat 0. Compare full policy/event/idempotency rows and actual successful Flyway history/checksums. Never use `clean`, edit applied migrations, or point this test at a user database. The later five-migration test remains unchanged.

**Practice 85:** 15 predict history, 35 run the supplied stage selector, 20 compare a count-only counterexample, 15 hand over the observation. **Selector:** `./mvnw -Plearner-tests -Dtest=LocalSchemaMigrationInfrastructureTest#day59VersionedBootstrapIsRepeatableAndNonDestructive test`.
