# Day 141 · 3 Mar 2027 — Refine customer dashboard and application cards (150m)

**Date:** 2027-03-03 · **Phase:** 1 · **Primary ID:** `CAP-2` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `ffbaeef875694a16445ac5a98d7181209cc5e33687cb9a784cef8aaf7b5ea7e6`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/OwnedApplicationCard.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/OwnedApplicationCardTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-2.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-2/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-2/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/OwnedApplicationCard.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-2.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-2` · primary · Day 141 · 3 Mar 2027 — Refine customer dashboard and application cards (150m)

Description SHA-256: `ffbaeef875694a16445ac5a98d7181209cc5e33687cb9a784cef8aaf7b5ea7e6`

**Practical task:** CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

Exact source description: [saved curriculum record `CAP-2`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Compose shared parameterised homepage journey/application/policy cards and wire an owner-scoped TanStack query after SPA session bootstrap. Keep server state, draft state and route state separate.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/OwnedApplicationCard.java
- src/test/java/capstone/coverstory/application/OwnedApplicationCardTest.java
- docs/coverstory/fixtures/cap-2.json
- docs/evidence/coverstory/CAP-2/result.json
- docs/evidence/coverstory/CAP-2/explain-back.md
- frontend/src/coverstory/application/OwnedApplicationCard.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-2.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named OwnedApplicationCard TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Compose shared parameterised homepage journey/application/policy cards and wire an owner-scoped TanStack query after SPA session bootstrap. Keep server state, draft state and route state separate.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Owned application props for onboarding, review and issued policy; explicit nextAction; no records and unknown status fixtures.

Output: Accessible prop-driven card with source-dated status and resume action; existing anonymous discovery entry remains usable.

#### Explicit acceptance cases — expected, not observed

CAP-2-CS-1
Given: A refreshes or starts a new session while session bootstrap is pending, then succeeds
Expected: Show bootstrap loading, then authenticated owned-journey refetch and allowed resume. No query runs under an unverified owner; no SSR hydration is assumed.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-2-CS-2
Given: Payment SUCCEEDED but policy absent
Expected: Show onboarding/resume state, never active cover.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-2-CS-3
Given: A logs out and B logs in while an old A query is in flight
Expected: Cancel/remove owner-scoped protected queries and clear rendered private state; late A response cannot populate B view. UUID knowledge and client analytics grant no access.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: SPA bootstrap and state-ownership graph at planned docs/architecture/coverstory/CAP-2-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): SessionBootstrap, OwnedJourneyQuery, MemoryQueryCache, JourneyCard, FormDraft, RouterState.

Change or annotate only these facts (maximum 3):
- Owned queries wait for authenticated session scope.
- Server projection, unsaved draft and URL state have separate owners.
- Logout/switch cancels and clears private state before the new identity renders.

Alternative to compare: Persist the private query cache and use it as refresh/session authority.

Interview prompt: A’s slow response returns after B logs in. Which boundaries prevent a flash or cache refill, and why is cold refetch not SSR hydration?

Failure evidence: CAP-2-CS-2 and CAP-2-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-184](https://dwait-to-adwait.youtrack.cloud/issue/CAP-184) — Day 140; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
