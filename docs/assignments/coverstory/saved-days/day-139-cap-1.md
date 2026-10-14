# Day 139 · 1 Mar 2027 — Audit and harden the existing CoverStory journey (150m)

**Date:** 2027-03-01 · **Phase:** 1 · **Primary ID:** `CAP-1` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `b7ab9ccadf9dea58cdb5a9c9428b41c530202914ab616e8d3bcce7cb6407670f`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Audit the already established target customer journey, verify the planned TanStack Query v5/session boundary and repair at most one demonstrated integration seam. Current repository product wiring remains an instructor prerequisite, not an implemented claim.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/ExistingJourneySeamAudit.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/ExistingJourneySeamAuditTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-1.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-1/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-1/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/ExistingJourneySeamAudit.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-1.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-1` · primary · Day 139 · 1 Mar 2027 — Audit and harden the existing CoverStory journey (150m)

Description SHA-256: `b7ab9ccadf9dea58cdb5a9c9428b41c530202914ab616e8d3bcce7cb6407670f`

**Practical task:** Audit the already established target customer journey, verify the planned TanStack Query v5/session boundary and repair at most one demonstrated integration seam. Current repository product wiring remains an instructor prerequisite, not an implemented claim.

Exact source description: [saved curriculum record `CAP-1`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Audit the already established target customer journey, verify the planned TanStack Query v5/session boundary and repair at most one demonstrated integration seam. Current repository product wiring remains an instructor prerequisite, not an implemented claim.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/ExistingJourneySeamAudit.java
- src/test/java/capstone/coverstory/application/ExistingJourneySeamAuditTest.java
- docs/coverstory/fixtures/cap-1.json
- docs/evidence/coverstory/CAP-1/result.json
- docs/evidence/coverstory/CAP-1/explain-back.md
- frontend/src/coverstory/application/ExistingJourneySeamAudit.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-1.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named ExistingJourneySeamAudit TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Audit the already established target customer journey, verify the planned TanStack Query v5/session boundary and repair at most one demonstrated integration seam. Current repository product wiring remains an instructor prerequisite, not an implemented claim.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Current package/runtime audit, prepared QueryClientProvider/session-bootstrap seam, actual existing frontend mechanisms, Day 138 API fixtures and one observed integration mismatch.

Output: Reproduction, bounded seam repair and before/after evidence with all unrelated behavior preserved.

#### Explicit acceptance cases — expected, not observed

CAP-1-CS-1
Given: Existing composed happy path is executed
Expected: Trace browser, Spring, module and PostgreSQL references; actual observed result is recorded.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-1-CS-2
Given: Prepared QueryClient and decoded authenticated query expose one real contract mismatch
Expected: Repair that bounded seam only; pin v5 configuration and rerun the regression. No new whole frontend or unverified installation is hidden in the task.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-1-CS-3
Given: Prerequisite journey cannot run or defect is unverified
Expected: BLOCKED/NOT_REPRODUCED; do not invent prior completion or create unrelated scaffolds as a substitute.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: actual frontend component and query-boundary audit at planned docs/architecture/coverstory/CAP-1-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): AppBoundary, QueryClientProvider, SessionBootstrap, DomainQueryHook, TypedViewModel, ExistingJourneyView.

Change or annotate only these facts (maximum 3):
- Distinguish actually reused mechanisms from planned product interfaces.
- A stable v5 query client sits behind resolved session scope.
- The day repairs one observed seam rather than introducing a whole new frontend.

Alternative to compare: Assume fixture-mode rendering proves a connected customer journey.

Interview prompt: What evidence proves the selected seam is broken, and what one change can repair it without pretending missing product wiring exists?

Failure evidence: CAP-1-CS-2 and CAP-1-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-164](https://dwait-to-adwait.youtrack.cloud/issue/CAP-164) — Day 138; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
