# Assistant-provided architecture notes: Days 127–140

These notes provide the capstone implementation decisions and reasons. The learner implements and tests the named seam. System-design practice stays in the separate interview question set; no capstone architecture proposal or post-hoc design justification is assigned.

## Day 127 — Scope one disposable rehearsal

Use run-issued fixture handles, not URL shape, to establish source and target ownership. Validate ownership and local-only mode before starting clients or processes. This keeps a mistaken identifier from reaching an unrelated database.

## Day 128 — Identify the existing release artifact

Bind the release record to the exact JAR bytes, source commit, tool versions, and each migration digest. Keep secrets out of the record. A filename or a successful build does not prove artifact identity.

## Day 129 — Define a full restore correctness oracle

Compare ordered row meaning, schema, migration history, and exact timestamps for the named synthetic states. Counts alone can hide substitutions or altered payloads. Keep the oracle separate from the restore command so it can reject a consistently wrong target.

## Day 130 — Capture a real local logical archive

Quiesce writes before `pg_dump`, run the fixed PostgreSQL 18.6 custom-archive command as an owned child process, then require exit zero, a nonempty archive, and a digest. A failed process or later write is not part of that recovery point.

## Day 131 — Restore only into a fresh empty target

Create a distinct `template0` target owned by the same run. Verify it is empty, restore the trusted archive in one transaction without `--clean` or `--create`, then compare schema and data. Transactional restore prevents a late archive error from leaving partial target objects.

## Day 132 — Verify the restored application, not just SQL

Only after full row/schema equality, run the existing executable JAR against the restored target and inspect a retained held record through the recovery-only CLI. The before/after snapshot proves the observation is read-only; health alone cannot prove the application reads restored state.

## Day 133 — Recovery checkpoint

Rerun only Days 127–132. Repair the earliest failed prerequisite and preserve later work as deferred. A process restart on the source database is not a database-restore rehearsal.

## Day 134 — Rehearse guarded configuration cutover

Keep A on source S selected until the same identified JAR, restored target T, readiness, schema, and full business-data checks pass. A failed gate leaves A selected. This protects the known-good path from an unverified candidate.

## Day 135 — Roll back configuration without rewinding data

Use the same binary and change only which database the test client selects. Return to S only while the candidate is quiescent and has accepted no divergent writes. Once T changes, switching to stale S would discard the accepted result.

## Day 136 — Measure recovery and own cleanup

Measure dump, restore, and verification with monotonic elapsed time and record fixture size/tool versions. Give every process, directory, and container a run-specific owner ID; stop only those resources and prove an unrelated sentinel remains.

## Day 137 — Draft the AWS resource and access boundary

Keep the design as offline data with cloud execution, resource creation, and grants all false. Model a private single-compute/private-database candidate and unresolved access choices without emitting deployable templates or making authorization implicit.

## Day 138 — Model cost and retained cleanup obligations

Represent each fictional cost line with currency, unit, rate, quantity, and amount. Require retained storage, backup, logs, secrets, transfer, and selected networking in the sum. Explicit units expose arithmetic mistakes; fictional rates must never be presented as a quote.

## Day 139 — Run the full rehearsal and write the cloud gate

Run the local archive, restore, packaged-JAR, cutover, and manual-recovery checks against the documented artifact and owned fixtures. Record cloud as unperformed and unauthorized. Local success cannot stand in for AWS evidence.

## Day 140 — Recovery and permission-gate review

Rerun prior Days 127–139 only. Keep real results, missing evidence, and cloud permission choices distinct. Do not add a deployment, feature, or new acceptance requirement on the recovery day.
