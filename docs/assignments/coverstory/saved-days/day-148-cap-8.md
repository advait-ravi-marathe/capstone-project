# Day 148 · 10 Mar 2027 — Deepen application and policy-detail navigation (150m)

**Date:** 2027-03-10 · **Phase:** 1 · **Primary ID:** `CAP-8` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `8694309e8b036c3f16b539cd162ca926d0f777ce21314e054379f81061072174`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/LifecycleDetailRoutes.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/LifecycleDetailRoutesTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-8.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-8/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-8/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/LifecycleDetailRoutes.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-8.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-8` · primary · Day 148 · 10 Mar 2027 — Deepen application and policy-detail navigation (150m)

Description SHA-256: `8694309e8b036c3f16b539cd162ca926d0f777ce21314e054379f81061072174`

**Practical task:** CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

Exact source description: [saved curriculum record `CAP-8`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Use owner/resource-keyed detail queries with addressable routes, preserving supported list filters and clearing stale resource/session details during navigation and refresh.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/LifecycleDetailRoutes.java
- src/test/java/capstone/coverstory/application/LifecycleDetailRoutesTest.java
- docs/coverstory/fixtures/cap-8.json
- docs/evidence/coverstory/CAP-8/result.json
- docs/evidence/coverstory/CAP-8/explain-back.md
- frontend/src/coverstory/application/LifecycleDetailRoutes.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-8.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named LifecycleDetailRoutes TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Use owner/resource-keyed detail queries with addressable routes, preserving supported list filters and clearing stale resource/session details during navigation and refresh.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: /applications/{A}, /policies/{pA}, direct refresh, A→B-shaped owned fixture switch, missing and foreign IDs.

Output: Correct detail/timeline/help section and safe Back/Forward/not-found behavior.

#### Explicit acceptance cases — expected, not observed

CAP-8-CS-1
Given: Owned detail is opened directly and refreshed
Expected: Same authoritative detail loads through prepared route fallback.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-8-CS-2
Given: List→owned detail1→owned detail2→Back
Expected: Restore detail1 and its timeline only; no stale detail2 fields.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-8-CS-3
Given: Missing or other-owner ID requested
Expected: Safe not-found/sign-in state with return path; clear prior private detail; no ownership inferred from URL.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: route/query/detail ownership graph at planned docs/architecture/coverstory/CAP-8-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): SafeDetailRoute, SessionScope, ResourceQueryKey, DetailQuery, TimelineQuery, BackForwardHistory.

Change or annotate only these facts (maximum 3):
- Owner/resource identity keys each detail query.
- Navigation resets stale detail before a new resource can render.
- Cold refresh performs bootstrap/refetch rather than relying on old component memory.

Alternative to compare: Keep one mutable detail object while switching route IDs.

Interview prompt: A direct deep link is opened after logout. Which state can the URL restore and which must come from authenticated server reads?

Failure evidence: CAP-8-CS-2 and CAP-8-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-5](https://dwait-to-adwait.youtrack.cloud/issue/CAP-5) — Day 144; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
- [CAP-185](https://dwait-to-adwait.youtrack.cloud/issue/CAP-185) — Day 147; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
