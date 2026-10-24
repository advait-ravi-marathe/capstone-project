# Day 59 — 2026-12-21: Inspect the additive versioned schema migration without resetting rows

## Contract

**Instructor-provided scaffold to inspect:** `Local Flyway startup migration`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Starting the packaged local app twice applies pending schema versions only; it preserves the synthetic policy row and history. No user database is migrated.

## Acceptance and expected TODO state

The instructor scaffold-verification selector is installed and passed; that result does not establish learner completion.

**Acceptance test:** `src/test/java/assignments/local/LocalSchemaMigrationInfrastructureTest.java`
**Acceptance selector:** `day59VersionedBootstrapIsRepeatableAndNonDestructive`
**Scaffold verification command:** `./mvnw -Plearner-tests -Dtest=LocalSchemaMigrationInfrastructureTest#day59VersionedBootstrapIsRepeatableAndNonDestructive test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner task — explain/debug acceptance

Run the packaged migration verification; inspect V001–V003 and explain migration order, checksums, the second no-op migrate, and why the sentinel row survives. Record how a future schema change would be additive.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no exact catalog match. No exact verified Flyway/schema-migration lesson was found in the inspected selected courses. Walk the supplied ordered migration files and schema-history row; show first application, repeat startup doing no duplicate migration, and checksum/version behavior without editing learner code.
- Focused worked references: [Flyway versioned migrations — versioned migrations and schema history](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/versioned-migrations).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Design sketch:** Inspect the additive versioned schema migration without resetting rows: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-59.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
