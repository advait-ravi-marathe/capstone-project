# Day 052 · 4 Dec 2026 — Fail closed on protected consumer payloads (150m)

**Date:** 2026-12-04 · **Phase:** 1 · **Primary ID:** `CAP-90` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `8dc88e482ba5868e3eca99da1472acf380e8dc30a03d1b79a288c651d0aff980`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Exercise the existing instructor-supplied protected-payload adapter and key-loading seam without plaintext fallback, secret leakage or homemade cryptography.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/payments/KeepMockAdapterCredentialsPrivate.java` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-90/fixtures.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-90/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-90/harness-self-check.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-90` · primary · Day 052 · 4 Dec 2026 — Fail closed on protected consumer payloads (150m)

Description SHA-256: `8dc88e482ba5868e3eca99da1472acf380e8dc30a03d1b79a288c651d0aff980`

**Practical task:** Exercise the existing instructor-supplied protected-payload adapter and key-loading seam without plaintext fallback, secret leakage or homemade cryptography.

Exact source description: [saved curriculum record `CAP-90`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### New daily outcome

Exercise the existing instructor-supplied protected-payload adapter and key-loading seam without plaintext fallback, secret leakage or homemade cryptography.

#### Instructor scaffold and planned interfaces

Canonical frontend preparation: TanStack React Query v5 owns server state under one stable QueryClient. Domain modules provide typed versioned query-key/options factories and public hooks; shared primitives receive typed data/actions and do not fetch across domain boundaries. Keys include relevant owner/resource/page/filter/sort inputs but never credentials or consumer form values. Owner-key partitioning is not server authorization. Forms and URL/router state remain separate; background refetch cannot overwrite unsaved input. Private query and mutation data is memory-only. A cold SPA refresh resolves session/bootstrap, shows truthful skeleton/signed-out/error UI and then authenticated refetches the current journey; no SSR, Next.js or TanStack Start is introduced. Pass AbortSignal into transport; logout/account switch cancels requests and clears private query/mutation caches and form state before the new identity renders, including protection from late old-owner responses. Use data-class freshness/garbage-collection/retry policies, no 401/403/validation retry loops and no blanket write retries. Server-confirmed mutations narrowly invalidate affected keys; refetch/page load never creates payment, submission or renewal commands. Optional future cache persistence is synthetic public-only, with version buster, finite maxAge and gcTime at least maxAge; private data and queued mutations are excluded. Tailwind semantic tokens and finite static class maps back shared parameterized Button, Field, EmptyState, Card, Dialog and typed compositions; verify the installed major and built CSS without silently upgrading. Proposed shared paths are frontend/src/coverstory/shared/ui/, frontend/src/coverstory/shared/theme/ and frontend/src/coverstory/shared/query/. Use tokenized Inter/system-ui, retain the Inter copyright/SIL OFL with any later distributed assets, and check accessible focus/labels/dialog behavior, narrow layout, zoom and font-display swap/failure. This is planned instructor preparation and NOT_RUN, not a documentation stack or extra learner project.

Deployment protection remains planned and unverified: authenticated TLS for browser/API and application/database connections, encrypted database volumes/backups, separate key access, authorized restore access and redacted application/proxy/error logs. Field encryption does not encrypt every index or metadata field; local loopback is not proof of deployment TLS or disk encryption. Ephemeral teaching keys last only for their test process. Cross-process restart/restore is BLOCKED unless an approved retained teaching-key mechanism is prepared and verified; missing keys return KEY_UNAVAILABLE with ciphertext retained. No infrastructure or security setting changes are authorized.

Instructor frontend prerequisite, proposed and not implemented: the thin existing React shell uses TanStack React Query for server state/cache and Tailwind with tokenized Inter/system-ui typography. Inter is the latest selected open-source font; the earlier Proxima Nova preference is superseded, with no font purchase or licensing blocker. Use shared parameterized accessible UI primitives and small journey compositions rather than one-off copied screens. Query cache is not form state or navigation state. Use typed, versioned query keys and targeted invalidation after relevant authenticated mutations; cancel and clear owner/session queries on logout or identity switch before rendering another owner. Never persist sensitive query results in browser storage. This SPA restores through authenticated refetch of authoritative journey/domain state after refresh, not SSR hydration. These are instructor scaffolds; the learner completes only the existing bounded demonstration seam.

New requirement prerequisite before the first private consumer write: instructor supplies the protected_payload_envelope adapter and ApplicationRevision link, server-owned journey/audit transaction adapters, and separate minimal analytics adapter. Do not postpone protection until CAP-90. Keys, crypto provider, routes, module transactions and React bindings are prepared and independently checked outside learner time; no implementation is asserted. Each targeted card replaces its former seam/cases within the same 95 minutes; missing preparation blocks the task.

Synthetic registry: docs/coverstory/fixtures/synthetic-registry-v1.json (planned repository-relative registry; not yet implemented). All acceptance examples remain embedded in this assignment.

Actual-repository prerequisite, reported by the parent on 2026-10-14: the current Spring scaffold exposes only the health route and scans capstone.app; the React scaffold is in fixture mode. Before any dependent learner task, the instructor must supply and verify the six-module bootstrap and component scanning for Catalog/Shortlist/Application/Payments/Policy/Support, public module interfaces, private persistence, and forbidden-import/cycle tests. JdbcEventDatabase remains the generic teaching harness; it is not a CoverStory domain repository. These additions are instructor preparation, not hidden learner work or an implemented baseline. Keep dependent work BLOCKED until code-owner reconciliation, smoke, independent oracle and timebox evidence exist.

Status: INSTRUCTOR_PERSISTENCE_SCAFFOLD_AUTHORED; LEARNER_ACCESS_TODO_RETAINED; KEY_RESTORE_BLOCKED.

Interface: loadProtectedAccess(KeyProvider,EnvelopeMetadata,AuthorizedObject)->EnvelopeAccess|SafeFailure; safeSummary()->RedactedConfig

Fixture: Synthetic authenticated-encryption tests and disposable PostgreSQL persistence now exercise an instructor JDBC envelope store linked by Application revision, owner-scoped lookup, append-only row protection and revision-bound AAD. Keys remain synthetic and process-local. No retained provider, wrapped DEK, cross-process restore, product authorization route or log/response integration is supplied; those missing runtimes still block the broader saved flow.

Implemented instructor extension paths: `src/main/java/coverstory/modules/application/persistence/PostgresProtectedPayloadEnvelopeStore.java` and `src/main/resources/db/schema/cap90_protected_payload.sql` (apply after `cap78_purchase.sql` in the disposable test DB). Runtime checks are `src/test/java/coverstory/platform/crypto/CAP90ProtectedEnvelopeRuntimeTest.java`; run with `mvn -Dtest=CAP90InstructorInfrastructureTest,CAP90ProtectedEnvelopeRuntimeTest test`. The database route was exercised directly through the Application-owned JDBC adapter; this does not wire an HTTP endpoint or make the learner `loadProtectedAccess` method work.

- src/main/java/capstone/coverstory/payments/KeepMockAdapterCredentialsPrivate.java
- frontend/src/coverstory/payments/ (instructor-owned thin existing-shell binding only)
- docs/evidence/coverstory/CAP-90/fixtures.json
- docs/evidence/coverstory/CAP-90/result.json
- docs/evidence/coverstory/CAP-90/harness-self-check.json

- Instructor prepares types, fixture loaders, fixed clocks, no-network fakes, thin React bindings, public module adapters and focused failing TODO tests; no complete learner solution.
- Reuse compatible Java 21/Maven/JUnit and embedded PostgreSQL harness after original oracle defects are independently corrected. No parallel Python database harness. Python pure functions remain Python where their original learning requires it.
- Supply an independent private reference/oracle check proving all cases jointly satisfiable, deliberate defect rejected and correct behavior accepted; raw test counts or expected TODO failures are insufficient.
- Setup smoke tests infrastructure independent of learner TODO; must remain green after a correct implementation and cannot require a permanent 501.
- Rehearse documentation 10 m, bounded learner build 55 m and tests/evidence 30 m. OVER_CAPACITY blocks assignment; rescope explicitly without dropping acceptance or adding catch-up work.
- Evidence records actual command, source commit, JDK/Maven/dependency versions, fixture and configuration hashes, expected/actual outputs and PASS/FAIL/BLOCKED/NOT_RUN. Proposed file existence is not evidence.

#### Learner TODO

Replace generic credential loading with bounded envelope/key metadata validation, fail-closed error mapping and redacted summaries. Invoke only supplied tested crypto/rotation adapters; do not implement AES, derive ad hoc keys, reuse IVs or create KMS access.

This replaces the former exercise seam and selected cases; it adds no learner time. Instructor supplies broad adapters before the session; preparation is a gate, not hidden learner work.

#### Inputs and outputs

ApplicationRevision references protected_payload_envelope containing ciphertext/tag, nonce, wrapped_dek and kek_reference; journey stores only the revision link. Proposed AES-256-GCM requires 12-byte IV and 128-bit tag; actual owner authorization precedes decrypt. AAD binds immutable opaque object ID plus object revision, payload kind and encryption schema version, never consumer fields. Missing/unavailable key, bad envelope/version/tag/ciphertext/AAD yields safe failure and zero partial plaintext/fallback. Key envelope revision can change without changing business identity or accepted contents. Logs/receipts contain configured booleans and stable codes only. Future KMS context is restricted to non-sensitive constant labels; no real KMS action. In-process round-trip, database restart, application-process restart and backup restore are distinct evidence modes. Ephemeral keys cannot establish cross-process recovery; retain ciphertext and report KEY_UNAVAILABLE unless a separately approved retained teaching-key mechanism is verified.

#### Acceptance cases

1. Given: Authorized synthetic envelope and available ephemeral key
Expected: Supplied adapter returns exact synthetic payload; database capture contains ciphertext/wrapped key only, no plaintext marker or wrapping key.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

2. Given: Missing/blank/unavailable key
Expected: KEY_UNAVAILABLE or CONFIG_MISSING; no empty-data success, plaintext persistence or fallback; summary exposes configured=false only.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

3. Given: Wrong owner requests a valid envelope
Expected: Deny before decrypt; provider/decrypt calls 0.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

4. Given: Modified tag, ciphertext, AAD object ID or object revision; malformed version/nonce/tag metadata
Expected: Safe authentication/envelope failure; partial plaintext and fallback 0; no marker in logs/errors.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

5. Given: Two encryptions under the same key outside test-vector mode
Expected: IVs are distinct. Fixed IV reuse is permitted only in isolated documented test vectors, never the normal adapter path.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

6. Given: Supplied in-process key-envelope rotation, followed by attempted cross-process restore of a retained backup
Expected: Rotation preserves object/business IDs and plaintext semantics in the declared process. Cross-process recovery is BLOCKED without a prepared and verified approved retained teaching-key mechanism; an absent ephemeral or old-version key returns KEY_UNAVAILABLE while ciphertext remains. Never call a database restart alone encrypted recovery.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

7. Given: Provider exception or effective manifest contains SECRET_SENTINEL
Expected: Response/log/receipt scan has zero sentinel bytes; no owner/journey/quote data inserted into proposed KMS context.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-90/result.json: named assertion and actual output; no sensitive payload

#### Architecture and interview delta

Extend the existing key-provider view with protected envelope, separate data/wrapping keys and authorized decrypt boundary. Explain why encryption never replaces owner checks and why key rotation cannot manufacture a new transaction.

At most 3 changed facts in the existing focused view; architecture at most 5 minutes within docs/evidence and interview at most 3 minutes within existing reflection. No additional time; Sunday remains zero mandatory DSM.

#### Earlier CAP dependencies

- CAP-83 (Day 044) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-83
- CAP-87 (Day 048) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-87
- CAP-88 (Day 050) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-88

Every dependency must supply reviewed evidence or a clearly bounded instructor fixture before dependent learner work begins; unresolved prerequisites are BLOCKED, not assumed complete.
