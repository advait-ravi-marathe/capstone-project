# Day 143 · 5 Mar 2027 — Handle lifecycle list loading and recovery states (150m)

**Date:** 2027-03-05 · **Phase:** 1 · **Primary ID:** `CAP-4` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `f8c94bc4392c3aea004ce825ee3eacf3d9ed0dfb0c6397e39b42e962e4c0a969`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Express TanStack query pending, empty, error, background-refetch and stale states explicitly while preserving safe session reset and best-effort analytics independence.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/ApplicationListState.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/ApplicationListStateTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-4.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-4/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-4/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/ApplicationListState.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-4.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-4` · primary · Day 143 · 5 Mar 2027 — Handle lifecycle list loading and recovery states (150m)

Description SHA-256: `f8c94bc4392c3aea004ce825ee3eacf3d9ed0dfb0c6397e39b42e962e4c0a969`

**Practical task:** Express TanStack query pending, empty, error, background-refetch and stale states explicitly while preserving safe session reset and best-effort analytics independence.

Exact source description: [saved curriculum record `CAP-4`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Express TanStack query pending, empty, error, background-refetch and stale states explicitly while preserving safe session reset and best-effort analytics independence.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/ApplicationListState.java
- src/test/java/capstone/coverstory/application/ApplicationListStateTest.java
- docs/coverstory/fixtures/cap-4.json
- docs/evidence/coverstory/CAP-4/result.json
- docs/evidence/coverstory/CAP-4/explain-back.md
- frontend/src/coverstory/application/ApplicationListState.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-4.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named ApplicationListState TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Express TanStack query pending, empty, error, background-refetch and stale states explicitly while preserving safe session reset and best-effort analytics independence.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Request held 500ms, empty array,503 requestId req-503 then successful retry, stale reversed response.

Output: Explicit accessible status and one deliberate retry; no blank list used as a substitute for an error.

#### Explicit acceptance cases — expected, not observed

CAP-4-CS-1
Given: Initial authenticated query stays unresolved for500ms
Expected: Loading appears; no fake empty list. On background refetch, only same-owner previously validated summaries may remain with an explicit stale indicator; sensitive fields are not persisted.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-4-CS-2
Given: Response is an actual empty authorized list
Expected: Empty guidance appears without implying all policies expired.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-4-CS-3
Given: List request fails with503 while analytics is blocked, then the user deliberately retries
Expected: Show safe failure/requestId, perform one retry and discard stale responses. Owned resume and already-authorized checkout remain independent of analytics ingestion.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: query loading/refetch/error state machine at planned docs/architecture/coverstory/CAP-4-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): NoDataPending, SuccessData, EmptyResult, BackgroundRefetch, SafeError, AnalyticsQueue.

Change or annotate only these facts (maximum 3):
- Initial loading differs from background refetch with same-owner validated data.
- Stale/error indicators are explicit and old responses are discarded.
- Analytics failure never gates essential reads or customer actions.

Alternative to compare: Render an empty list on every failed request and block UI until analytics succeeds.

Interview prompt: A background refetch fails while an old summary is visible. What may remain, what must be labelled stale, and what must not be inferred?

Failure evidence: CAP-4-CS-2 and CAP-4-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-184](https://dwait-to-adwait.youtrack.cloud/issue/CAP-184) — Day 140; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
- [CAP-2](https://dwait-to-adwait.youtrack.cloud/issue/CAP-2) — Day 141; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
