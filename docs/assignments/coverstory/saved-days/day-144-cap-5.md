# Day 144 · 6 Mar 2027 — Harden owner-scoped customer API reads (150m)

**Date:** 2027-03-06 · **Phase:** 1 · **Primary ID:** `CAP-5` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `7f571f1e2c515edc1c6165a9e0347785a14e1ba1c9df6fef2766e9619dcae7b5`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Complete the decoded owner-scoped TanStack query adapter using its cancellation signal, authenticated session scope and explicit retry policy; clear private cached state at auth boundaries.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/OwnedApplicationApiAdapter.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/OwnedApplicationApiAdapterTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-5.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-5/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-5/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/OwnedApplicationApiAdapter.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-5.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-5` · primary · Day 144 · 6 Mar 2027 — Harden owner-scoped customer API reads (150m)

Description SHA-256: `7f571f1e2c515edc1c6165a9e0347785a14e1ba1c9df6fef2766e9619dcae7b5`

**Practical task:** Complete the decoded owner-scoped TanStack query adapter using its cancellation signal, authenticated session scope and explicit retry policy; clear private cached state at auth boundaries.

Exact source description: [saved curriculum record `CAP-5`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Complete the decoded owner-scoped TanStack query adapter using its cancellation signal, authenticated session scope and explicit retry policy; clear private cached state at auth boundaries.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/OwnedApplicationApiAdapter.java
- src/test/java/capstone/coverstory/application/OwnedApplicationApiAdapterTest.java
- docs/coverstory/fixtures/cap-5.json
- docs/evidence/coverstory/CAP-5/result.json
- docs/evidence/coverstory/CAP-5/explain-back.md
- frontend/src/coverstory/application/OwnedApplicationApiAdapter.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-5.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named OwnedApplicationApiAdapter TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Complete the decoded owner-scoped TanStack query adapter using its cancellation signal, authenticated session scope and explicit retry policy; clear private cached state at auth boundaries.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Synthetic A session; owned A/B journeys and application links; GET /api/v1/me/journeys and existing application read adapter; 401, safe404 and malformed 200. The early segment owns POST /api/v1/analytics/events; consume it without defining a second endpoint.

Output: A-only rows, expired/denied/contract-error states and cancellation of obsolete fetches.

#### Explicit acceptance cases — expected, not observed

CAP-5-CS-1
Given: A lists current journeys while A and B records exist
Expected: Response/UI contain only A minimal summaries; application/quote/revision ownership is checked through module interfaces; UUID and client filters grant no access.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-5-CS-2
Given: Session expires with 401 or owner scope changes during an in-flight request
Expected: Stop auth-error retries, cancel/remove protected queries and clear visible data. Late responses cannot cross the session boundary; show sign-in-required state.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-5-CS-3
Given: 200 omits application ID or amount/currency invariant
Expected: Safe contract error with request reference; no clickable broken row or guessed money.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: owner-scoped query adapter sequence at planned docs/architecture/coverstory/CAP-5-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): SessionScope, QueryKeyFactory, DomainQueryFn, AbortSignalTransport, RuntimeDecoder, ServerOwnerBoundary.

Change or annotate only these facts (maximum 3):
- Keys partition by all data-changing inputs but never authorize access.
- AbortSignal reaches the real transport.
- Auth failures stop retries and remove old private cache/rendered data.

Alternative to compare: Cancel only component rendering while allowing old requests to refill a shared cache.

Interview prompt: Why are owner-partitioned keys insufficient without server authorization and session-change cancellation?

Failure evidence: CAP-5-CS-2 and CAP-5-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-184](https://dwait-to-adwait.youtrack.cloud/issue/CAP-184) — Day 140; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
- [CAP-4](https://dwait-to-adwait.youtrack.cloud/issue/CAP-4) — Day 143; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
