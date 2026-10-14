# Day 039 · 21 Nov 2026 — Reject stale CoverStory source evidence (150m)

**Date:** 2026-11-21 · **Phase:** 1 · **Primary ID:** `CAP-79` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `a580055898e2304e507a36df5305b17a49ca42f5e5a49a08368a87997718ed47`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Apply a fixed-clock freshness filter to source excerpts shown beside a health quote or read-only help answer; do not relabel an illustration as a payable quote.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_79.py` (planned in source; existence/readiness is not implied)
- `src/main/java/capstone/coverstory/catalog/RejectStaleCoverstorySourceEvidence.java` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-79/fixtures.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-79/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-79/harness-self-check.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-79` · primary · Day 039 · 21 Nov 2026 — Reject stale CoverStory source evidence (150m)

Description SHA-256: `a580055898e2304e507a36df5305b17a49ca42f5e5a49a08368a87997718ed47`

**Practical task:** Apply a fixed-clock freshness filter to source excerpts shown beside a health quote or read-only help answer; do not relabel an illustration as a payable quote.

Exact source description: [saved curriculum record `CAP-79`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### New daily outcome

Apply a fixed-clock freshness filter to source excerpts shown beside a health quote or read-only help answer; do not relabel an illustration as a payable quote.

#### Instructor scaffold and planned interfaces

Canonical frontend preparation: TanStack React Query v5 owns server state under one stable QueryClient. Domain modules provide typed versioned query-key/options factories and public hooks; shared primitives receive typed data/actions and do not fetch across domain boundaries. Keys include relevant owner/resource/page/filter/sort inputs but never credentials or consumer form values. Owner-key partitioning is not server authorization. Forms and URL/router state remain separate; background refetch cannot overwrite unsaved input. Private query and mutation data is memory-only. A cold SPA refresh resolves session/bootstrap, shows truthful skeleton/signed-out/error UI and then authenticated refetches the current journey; no SSR, Next.js or TanStack Start is introduced. Pass AbortSignal into transport; logout/account switch cancels requests and clears private query/mutation caches and form state before the new identity renders, including protection from late old-owner responses. Use data-class freshness/garbage-collection/retry policies, no 401/403/validation retry loops and no blanket write retries. Server-confirmed mutations narrowly invalidate affected keys; refetch/page load never creates payment, submission or renewal commands. Optional future cache persistence is synthetic public-only, with version buster, finite maxAge and gcTime at least maxAge; private data and queued mutations are excluded. Tailwind semantic tokens and finite static class maps back shared parameterized Button, Field, EmptyState, Card, Dialog and typed compositions; verify the installed major and built CSS without silently upgrading. Proposed shared paths are frontend/src/coverstory/shared/ui/, frontend/src/coverstory/shared/theme/ and frontend/src/coverstory/shared/query/. Use tokenized Inter/system-ui, retain the Inter copyright/SIL OFL with any later distributed assets, and check accessible focus/labels/dialog behavior, narrow layout, zoom and font-display swap/failure. This is planned instructor preparation and NOT_RUN, not a documentation stack or extra learner project.

Deployment protection remains planned and unverified: authenticated TLS for browser/API and application/database connections, encrypted database volumes/backups, separate key access, authorized restore access and redacted application/proxy/error logs. Field encryption does not encrypt every index or metadata field; local loopback is not proof of deployment TLS or disk encryption. Ephemeral teaching keys last only for their test process. Cross-process restart/restore is BLOCKED unless an approved retained teaching-key mechanism is prepared and verified; missing keys return KEY_UNAVAILABLE with ciphertext retained. No infrastructure or security setting changes are authorized.

Instructor frontend prerequisite, proposed and not implemented: the thin existing React shell uses TanStack React Query for server state/cache and Tailwind with tokenized Inter/system-ui typography. Inter is the latest selected open-source font; the earlier Proxima Nova preference is superseded, with no font purchase or licensing blocker. Use shared parameterized accessible UI primitives and small journey compositions rather than one-off copied screens. Query cache is not form state or navigation state. Use typed, versioned query keys and targeted invalidation after relevant authenticated mutations; cancel and clear owner/session queries on logout or identity switch before rendering another owner. Never persist sensitive query results in browser storage. This SPA restores through authenticated refetch of authoritative journey/domain state after refresh, not SSR hydration. These are instructor scaffolds; the learner completes only the existing bounded demonstration seam.

New requirement prerequisite before the first private consumer write: instructor supplies the protected_payload_envelope adapter and ApplicationRevision link, server-owned journey/audit transaction adapters, and separate minimal analytics adapter. Do not postpone protection until CAP-90. Keys, crypto provider, routes, module transactions and React bindings are prepared and independently checked outside learner time; no implementation is asserted. Each targeted card replaces its former seam/cases within the same 95 minutes; missing preparation blocks the task.

Synthetic registry: docs/coverstory/fixtures/synthetic-registry-v1.json (planned repository-relative registry; not yet implemented). All acceptance examples remain embedded in this assignment.

Actual-repository prerequisite, reported by the parent on 2026-10-14: the current Spring scaffold exposes only the health route and scans capstone.app; the React scaffold is in fixture mode. Before any dependent learner task, the instructor must supply and verify the six-module bootstrap and component scanning for Catalog/Shortlist/Application/Payments/Policy/Support, public module interfaces, private persistence, and forbidden-import/cycle tests. JdbcEventDatabase remains the generic teaching harness; it is not a CoverStory domain repository. These additions are instructor preparation, not hidden learner work or an implemented baseline. Keep dependent work BLOCKED until code-owner reconciliation, smoke, independent oracle and timebox evidence exist.

Status: PROPOSED_NOT_IMPLEMENTED.

Interface: filter_fresh(hits:list[SourceHit], now:awareUTC, max_age:timedelta)->FreshnessResult

Fixture: Versioned SYNTHETIC health-plan-A-v1/v2 excerpts; fixed now=2026-11-21T12:00:00Z; 24-hour fixture freshness; allowed customer-A/public source set.

- ai/coverstory/cap_79.py
- src/main/java/capstone/coverstory/catalog/RejectStaleCoverstorySourceEvidence.java
- frontend/src/coverstory/catalog/ (instructor-owned thin existing-shell binding only)
- docs/evidence/coverstory/CAP-79/fixtures.json
- docs/evidence/coverstory/CAP-79/result.json
- docs/evidence/coverstory/CAP-79/harness-self-check.json

- Instructor prepares types, fixture loaders, fixed clocks, no-network fakes, thin React bindings, public module adapters and focused failing TODO tests; no complete learner solution.
- Reuse compatible Java 21/Maven/JUnit and embedded PostgreSQL harness after original oracle defects are independently corrected. No parallel Python database harness. Python pure functions remain Python where their original learning requires it.
- Supply an independent private reference/oracle check proving all cases jointly satisfiable, deliberate defect rejected and correct behavior accepted; raw test counts or expected TODO failures are insufficient.
- Setup smoke tests infrastructure independent of learner TODO; must remain green after a correct implementation and cannot require a permanent 501.
- Rehearse documentation 10 m, bounded learner build 55 m and tests/evidence 30 m. OVER_CAPACITY blocks assignment; rescope explicitly without dropping acceptance or adding catch-up work.
- Evidence records actual command, source commit, JDK/Maven/dependency versions, fixture and configuration hashes, expected/actual outputs and PASS/FAIL/BLOCKED/NOT_RUN. Proposed file existence is not evidence.

#### Learner TODO

Implement only freshness validation and reason codes; render its supplied result component in the existing React shell.

Implement only this seam and focused assertions. Do not change fixtures, expected results, owner rules, source identity, public interfaces or dependencies to make tests pass. No solution implementation supplied.

#### Inputs and outputs

SourceHit includes sourceId, version, exact digest, evidenceType and aware timestamp. Eligibility is 0<=age<=24 h; future/naive times INVALID_TIME; all excluded gives ABSTAIN. This internal recheck rule is separate from source_valid_until and does not establish payable eligibility.

#### Acceptance cases

1. Given: 12 h-old allowed source
Expected: Retain original source/version and synthetic label.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-79/result.json: named assertion plus observed output

2. Given: Exactly 24 h old; then 24 h+1 s
Expected: Retain boundary; exclude older row as STALE.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-79/result.json: named assertion plus observed output

3. Given: Future timestamp or timezone absent
Expected: INVALID_TIME before age arithmetic; no answer.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-79/result.json: named assertion plus observed output

4. Given: v1 stale and v2 fresh
Expected: Only v2 citation; v1 is not silently relabelled.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-79/result.json: named assertion plus observed output

5. Given: Fresh advertised illustration
Expected: May show labelled evidence; quote/order eligibility remains false.
Execution: NOT_RUN. Evidence: docs/evidence/coverstory/CAP-79/result.json: named assertion plus observed output

#### Architecture and interview delta

Annotate source identity, recheck freshness and payable eligibility as three distinct facts; explain why fresh does not mean trustworthy or payable.

At most 3 changed facts in an existing <=6-entity or <=10-message view; <=5 m from existing docs/evidence. Interview<=3 m from existing DSM/design reflection. Explain one alternative and unseen failure; use actual case evidence, no invented measurement.

#### Earlier CAP dependencies

- CAP-166 (Day 014) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-166
- CAP-168 (Day 028) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-168
- CAP-78 (Day 038) · https://dwait-to-adwait.youtrack.cloud/issue/CAP-78

Every dependency must supply reviewed evidence or a clearly bounded instructor fixture before dependent learner work begins; unresolved prerequisites are BLOCKED, not assumed complete.
