# Day 048 · 30 Nov 2026 — Demonstrate encrypted owned journey resume (150m)

**Date:** 2026-11-30 · **Phase:** 1 · **Primary ID:** `CAP-87` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `e77659da69bbc76ce021ddbaccf07221806208f3be1c5a3bfa68c26945e344d8`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Demonstrate the prepared React checkout/onboarding slice with encrypted private drafts, authoritative journey history, owner-authenticated homepage resume and analytics failure independence.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/DemonstratePaidOnboardingAndResume.java` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-87/fixtures.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-87/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-87/harness-self-check.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-87` · primary · Day 048 · 30 Nov 2026 — Demonstrate encrypted owned journey resume (150m)

Description SHA-256: `e77659da69bbc76ce021ddbaccf07221806208f3be1c5a3bfa68c26945e344d8`

**Practical task:** Demonstrate the prepared React checkout/onboarding slice with encrypted private drafts, authoritative journey history, owner-authenticated homepage resume and analytics failure independence.

Exact source description: [saved curriculum record `CAP-87`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### New daily outcome

Demonstrate the prepared React checkout/onboarding slice with encrypted private drafts, authoritative journey history, owner-authenticated homepage resume and analytics failure independence.

#### Instructor scaffold and planned interfaces

Canonical frontend preparation: TanStack React Query v5 owns server state under one stable QueryClient. Domain modules provide typed versioned query-key/options factories and public hooks; shared primitives receive typed data/actions and do not fetch across domain boundaries. Keys include relevant owner/resource/page/filter/sort inputs but never credentials or consumer form values. Owner-key partitioning is not server authorization. Forms and URL/router state remain separate; background refetch cannot overwrite unsaved input. Private query and mutation data is memory-only. A cold SPA refresh resolves session/bootstrap, shows truthful skeleton/signed-out/error UI and then authenticated refetches the current journey; no SSR, Next.js or TanStack Start is introduced. Pass AbortSignal into transport; logout/account switch cancels requests and clears private query/mutation caches and form state before the new identity renders, including protection from late old-owner responses. Use data-class freshness/garbage-collection/retry policies, no 401/403/validation retry loops and no blanket write retries. Server-confirmed mutations narrowly invalidate affected keys; refetch/page load never creates payment, submission or renewal commands. Optional future cache persistence is synthetic public-only, with version buster, finite maxAge and gcTime at least maxAge; private data and queued mutations are excluded. Tailwind semantic tokens and finite static class maps back shared parameterized Button, Field, EmptyState, Card, Dialog and typed compositions; verify the installed major and built CSS without silently upgrading. Proposed shared paths are frontend/src/coverstory/shared/ui/, frontend/src/coverstory/shared/theme/ and frontend/src/coverstory/shared/query/. Use tokenized Inter/system-ui, retain the Inter copyright/SIL OFL with any later distributed assets, and check accessible focus/labels/dialog behavior, narrow layout, zoom and font-display swap/failure. This is planned instructor preparation and NOT_RUN, not a documentation stack or extra learner project.

Deployment protection remains planned and unverified: authenticated TLS for browser/API and application/database connections, encrypted database volumes/backups, separate key access, authorized restore access and redacted application/proxy/error logs. Field encryption does not encrypt every index or metadata field; local loopback is not proof of deployment TLS or disk encryption. Ephemeral teaching keys last only for their test process. Cross-process restart/restore is BLOCKED unless an approved retained teaching-key mechanism is prepared and verified; missing keys return KEY_UNAVAILABLE with ciphertext retained. No infrastructure or security setting changes are authorized.

Instructor frontend prerequisite, proposed and not implemented: the thin existing React shell uses TanStack React Query for server state/cache and Tailwind with tokenized Inter/system-ui typography. Inter is the latest selected open-source font; the earlier Proxima Nova preference is superseded, with no font purchase or licensing blocker. Use shared parameterized accessible UI primitives and small journey compositions rather than one-off copied screens. Query cache is not form state or navigation state. Use typed, versioned query keys and targeted invalidation after relevant authenticated mutations; cancel and clear owner/session queries on logout or identity switch before rendering another owner. Never persist sensitive query results in browser storage. This SPA restores through authenticated refetch of authoritative journey/domain state after refresh, not SSR hydration. These are instructor scaffolds; the learner completes only the existing bounded demonstration seam.

New requirement prerequisite before the first private consumer write: instructor supplies the protected_payload_envelope adapter and ApplicationRevision link, server-owned journey/audit transaction adapters, and separate minimal analytics adapter. Do not postpone protection until CAP-90. Keys, crypto provider, routes, module transactions and React bindings are prepared and independently checked outside learner time; no implementation is asserted. Each targeted card replaces its former seam/cases within the same 95 minutes; missing preparation blocks the task.

Synthetic registry: docs/coverstory/fixtures/synthetic-registry-v1.json (planned repository-relative registry; not yet implemented). All acceptance examples remain embedded in this assignment.

Actual-repository prerequisite, reported by the parent on 2026-10-14: the current Spring scaffold exposes only the health route and scans capstone.app; the React scaffold is in fixture mode. Before any dependent learner task, the instructor must supply and verify the six-module bootstrap and component scanning for Catalog/Shortlist/Application/Payments/Policy/Support, public module interfaces, private persistence, and forbidden-import/cycle tests. JdbcEventDatabase remains the generic teaching harness; it is not a CoverStory domain repository. These additions are instructor preparation, not hidden learner work or an implemented baseline. Keep dependent work BLOCKED until code-owner reconciliation, smoke, independent oracle and timebox evidence exist.

Status: PROPOSED_NOT_IMPLEMENTED.

Interface: runOwnedJourneyDemo(ScenarioFixture,PreparedCoverStoryClient,ReactDriver)->DemoReport

Fixture: Existing instructor React shell, A/B sessions, mock Juspay, application-A revision 2, exact quote revision 2, fixed journey alias jA, maintained encryption adapter, server transaction/audit/journey adapters, telemetry allowlist and bounded fake transport. Registry aliases map to fixed synthetic UUIDs; no real consumer data. TanStack Query test client, typed/versioned owner/session query keys, logout/switch cancellation spies, Tailwind tokens, Inter/system-ui font stack and shared parameterized primitive/composition fixtures are instructor supplied. Exact added registry aliases: journey-A=88888888-8888-4888-8888-000000000001, journey-B=88888888-8888-4888-8888-000000000002; analytics-e1=99999999-9999-4999-8999-000000000001, analytics-e2=99999999-9999-4999-8999-000000000002; envelope-application-A-revision2=aaaaaaaa-aaaa-4aaa-8aaa-000000000001. jA/jB are explicit journey-A/B aliases; revisions retain the composite (application_id, revision) key.

- src/main/java/capstone/coverstory/application/DemonstratePaidOnboardingAndResume.java
- frontend/src/coverstory/application/ (instructor-owned thin existing-shell binding only)
- docs/evidence/coverstory/CAP-87/fixtures.json
- docs/evidence/coverstory/CAP-87/result.json
- docs/evidence/coverstory/CAP-87/harness-self-check.json

- Instructor prepares types, fixture loaders, fixed clocks, no-network fakes, thin React bindings, public module adapters and focused failing TODO tests; no complete learner solution.
- Reuse compatible Java 21/Maven/JUnit and embedded PostgreSQL harness after original oracle defects are independently corrected. No parallel Python database harness. Python pure functions remain Python where their original learning requires it.
- Supply an independent private reference/oracle check proving all cases jointly satisfiable, deliberate defect rejected and correct behavior accepted; raw test counts or expected TODO failures are insufficient.
- Setup smoke tests infrastructure independent of learner TODO; must remain green after a correct implementation and cannot require a permanent 501.
- Rehearse documentation 10 m, bounded learner build 55 m and tests/evidence 30 m. OVER_CAPACITY blocks assignment; rescope explicitly without dropping acceptance or adding catch-up work.
- Evidence records actual command, source commit, JDK/Maven/dependency versions, fixture and configuration hashes, expected/actual outputs and PASS/FAIL/BLOCKED/NOT_RUN. Proposed file existence is not evidence.

#### Learner TODO

Replace the optional cited-help and repeated reconciliation-probe demonstrations with protected persistence, authoritative journey resume and telemetry-independence assertions. Fill only prepared demo orchestration/assertion seams; do not build a new UI, encryption system, analytics service or journey implementation. Use the supplied query-client/React driver to assert refresh and identity-switch behavior; no frontend framework setup or component-system implementation is added.

This replaces the former exercise seam and selected cases; it adds no learner time. Instructor supplies broad adapters before the session; preparation is a gate, not hidden learner work.

#### Inputs and outputs

Public comparison/device-local shortlist→login and version revalidation→owned order/mock confirmation→ApplicationRevision linked to protected_payload_envelope→one atomic authoritative audit/journey transition→GET /api/v1/me/journeys and GET /api/v1/journeys/{id} for allowed resume action. Homepage responses contain minimal navigation metadata, not decrypted consumer fields. The prepared PAGE_VIEW/CONTROL_CLICK adapter uses the canonical bounded analytics API; no browser event grants business authority. Payment confirmation still unlocks ONBOARDING only, never insurer dispatch. React Query holds transient server data only; unsaved forms and navigation use their own state. After refresh, authenticated refetch supplies the authoritative journey step. Logout/account switch cancels and clears the previous owner’s queries; no sensitive persisted browser cache or SSR hydration claim. A page load or query refetch has no payment/submission/renewal mutation side effect. Failed mutations retain unsaved forms and use narrow server-confirmed invalidation; 401/403/validation responses never retry in a loop. Query keys exclude consumer fields and credentials.

#### Acceptance cases

1. Given: A confirms a mock payment, saves an encrypted synthetic draft and reloads in a fresh authenticated session
Expected: One payment effect and application; matching audit and journey step; homepage returns jA at its server-valid step. Persisted private values exist only in the protected envelope, not journey or telemetry. A SPA refresh performs authenticated refetch; query cache never substitutes for the server version. Bootstrap resolves session before private fetch, presents a truthful loading/error state, and preserves safe deep-link filters and unsaved input.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

2. Given: B guesses jA, or anonymous visitor requests private resume
Expected: Safe 404 for foreign/unknown authenticated IDs; unauthenticated access requires login; decrypt/provider calls 0. Anonymous discovery remains device-local. Switching from A to B cancels and clears A query data before rendering; no A-content flash or sensitive browser persistence. AbortSignal reaches the transport; old mutation/form state is discarded and a late A response cannot populate B UI.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

3. Given: Duplicate successful callback or retry after response loss
Expected: One money effect and one authoritative journey advance; retain safe diagnostic observation without duplicate posted amount.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

4. Given: Analytics blocked/unavailable, queue full at 20 events, or retry reaches 3 attempts
Expected: Already-authorized checkout, draft save and owned resume still succeed; telemetry may drop and cannot change journey version.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

5. Given: Exact PAGE_VIEW e1 repeated, then the same e1 with a different valid page_id; PAGE_VIEW with target_id tested separately
Expected: One telemetry row; 202 RECEIVED then DUPLICATE; changed valid payload returns 409 EVENT_ID_CONFLICT. PAGE_VIEW with target_id is invalid and returns 400. Client timestamp order never changes journey state.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

6. Given: CONTROL_CLICK contains email, healthAnswer, amountMinor, rawUrl, token or arbitrary properties; or links B journey
Expected: Reject invalid or unauthorized data safely; no secret-marker bytes in persisted telemetry, errors or logs. Policy-off client emits nothing and defensive arrivals receive DISCARDED_BY_POLICY without payload storage.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

7. Given: Stale browser claims ISSUED; or required prepared dependency is absent
Expected: No state-setting command exists; server recomputes allowed actions. Missing dependency yields BLOCKED, not fabricated PASS. Page refresh/refetch never triggers a payment or submit command.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-87/result.json: named assertion and actual output; no sensitive payload

#### Architecture and interview delta

Reuse the browser→Application→PostgreSQL sequence: encrypted domain revision and authoritative journey/audit commit together; lossy telemetry has a separate failure path. Defend why a UUID and a page click cannot establish identity or state.

At most 3 changed facts in the existing focused view; architecture at most 5 minutes within docs/evidence and interview at most 3 minutes within existing reflection. No additional time; Sunday remains zero mandatory DSM.

#### Earlier CAP dependencies

- CAP-166 (Day 014) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-166
- CAP-168 (Day 028) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-168
- CAP-80 (Day 040) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-80
- CAP-83 (Day 044) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-83
- CAP-84 (Day 045) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-84
- CAP-85 (Day 046) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-85
- CAP-86 (Day 047) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-86

Every dependency must supply reviewed evidence or a clearly bounded instructor fixture before dependent learner work begins; unresolved prerequisites are BLOCKED, not assumed complete.
