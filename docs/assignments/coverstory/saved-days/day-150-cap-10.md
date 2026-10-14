# Day 150 · 12 Mar 2027 — Confirm recovery and cancellation actions accessibly (150m)

**Date:** 2027-03-12 · **Phase:** 1 · **Primary ID:** `CAP-10` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `7243c407c4f11d04dd0d1037468d21dfd6758a4569c14566a9e221d36bc0aadd`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/LifecycleConfirmationDialog.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/LifecycleConfirmationDialogTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-10.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-10/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-10/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/LifecycleConfirmationDialog.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-10.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-10` · primary · Day 150 · 12 Mar 2027 — Confirm recovery and cancellation actions accessibly (150m)

Description SHA-256: `7243c407c4f11d04dd0d1037468d21dfd6758a4569c14566a9e221d36bc0aadd`

**Practical task:** CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

Exact source description: [saved curriculum record `CAP-10`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Apply the existing deliberate-confirmation UI skill to named recoverable mock dispatch work and pre-submission cancellation, never automatic money replay.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/LifecycleConfirmationDialog.java
- src/test/java/capstone/coverstory/application/LifecycleConfirmationDialogTest.java
- docs/coverstory/fixtures/cap-10.json
- docs/evidence/coverstory/CAP-10/result.json
- docs/evidence/coverstory/CAP-10/explain-back.md
- frontend/src/coverstory/application/LifecycleConfirmationDialog.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-10.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named LifecycleConfirmationDialog TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Apply the existing deliberate-confirmation UI skill to named recoverable mock dispatch work and pre-submission cancellation, never automatic money replay.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Selected application/job reference, server eligibility/reason, effect statement, Tab/Shift+Tab/Escape, double confirmation and stale selection.

Output: Accessible reusable modal with one explicit eligible callback and zero mutation on opening/dismissal.

#### Explicit acceptance cases — expected, not observed

CAP-10-CS-1
Given: Dialog opens then Escape
Expected: Closes, focus returns to trigger, mutation count 0.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-10-CS-2
Given: Keyboard traverses modal or selected application changes beneath it
Expected: Focus remains reachable/trapped appropriately; captured target revalidated, no wrong-application mutation.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-10-CS-3
Given: Confirm activated twice while pending or eligibility false
Expected: One callback only when eligible; false eligibility or unknown dispatch blocks and offers reconciliation/help.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: confirmation-dialog interaction state machine at planned docs/architecture/coverstory/CAP-10-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): Trigger, CapturedTargetVersion, ModalFocusState, EligibilityResult, ConfirmMutation, FocusReturn.

Change or annotate only these facts (maximum 3):
- Opening/dismissal never triggers mutation.
- Target/version and server eligibility are rechecked on deliberate confirmation.
- Focus containment and return are part of the functional contract.

Alternative to compare: Bind confirmation to whichever application happens to be selected when the callback eventually runs.

Interview prompt: Selection changes while the modal is open. How do you prevent a wrong-target cancellation or recovery and preserve keyboard behavior?

Failure evidence: CAP-10-CS-2 and CAP-10-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-129](https://dwait-to-adwait.youtrack.cloud/issue/CAP-129) — Day 097; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
- [CAP-9](https://dwait-to-adwait.youtrack.cloud/issue/CAP-9) — Day 149; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
