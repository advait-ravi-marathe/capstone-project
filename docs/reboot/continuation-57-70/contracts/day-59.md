# Day 59 — 2026-12-21: Inspect the additive versioned schema migration without resetting rows

## Contract

**Instructor-provided scaffold to inspect:** `Local Flyway startup migration`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

A fresh disposable PostgreSQL database applies only V001–V003, then a second Flyway migrate is a no-op. An inserted sentinel and the exact successful version/checksum history remain unchanged. No user database is migrated.

## Acceptance and expected TODO state

**Acceptance harness:** supplied Sprint5-scoped verification

A fresh embedded PostgreSQL database migrates V001–V003 exactly once (3), retains a sentinel and exact successful version/checksum history, then reports 0 migrations and unchanged rows/history. The separate V001–V005 test remains intact.

**Acceptance selector:** `day59VersionedBootstrapIsRepeatableAndNonDestructive`
**Acceptance source:** `src/test/java/assignments/local/LocalSchemaMigrationInfrastructureTest.java`
**Command:**

```sh
./mvnw -Plearner-tests -Dtest=LocalSchemaMigrationInfrastructureTest#day59VersionedBootstrapIsRepeatableAndNonDestructive test
```

**Deferred-work key:** `S5-D59-EXPLAIN` — Record the actual migration trace and explain target/version/history evidence; no learner migration implementation is assigned.

## Learner task — explain/debug acceptance

Run the isolated target-3 selector; inspect V001–V003 and explain migration order, checksums, the second no-op migrate, and why the sentinel row survives. The five-migration V001–V005 test is a separate later-slice check.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Learner-first task proposal (5 minutes within the 85-minute practice block)

Before opening migration guidance, predict first-run migration count, repeat-run count, and whether a reader-created sentinel and schema history should change. Identify which existing test is five migrations and why it cannot prove target 3.

Write the prediction before opening references, worked examples, hints, or AI. Spend the remaining 80 minutes on the existing acceptance only; this proposal is not completion evidence. The selector is instructor-provided scaffold evidence; explain the target-3 migration trace without claiming an unobserved run.

## Selector limits and open evidence

A fresh embedded PostgreSQL database migrates V001–V003 exactly once (3), retains a sentinel and exact successful version/checksum history, then reports 0 migrations and unchanged rows/history. The separate V001–V005 test remains intact. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: no exact catalog match. No exact verified Flyway/schema-migration lesson was found in the inspected selected courses. Walk the supplied ordered migration files and schema-history row; show first application, repeat startup doing no duplicate migration, and checksum/version behavior without editing learner code.
- Focused worked references: [Flyway versioned migrations — versioned migrations and schema history](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/versioned-migrations).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Complete the task-specific proposal above before guidance; do not add a post-hoc architecture essay.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-59.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
