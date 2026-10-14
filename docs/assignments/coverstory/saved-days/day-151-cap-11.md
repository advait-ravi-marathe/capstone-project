# Day 151 · 13 Mar 2027 — Display bounded recovery and reconciliation outcomes (150m)

**Date:** 2027-03-13 · **Phase:** 1 · **Primary ID:** `CAP-11` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `6bba100cd2ceeafd76b1d818b0f5cd36a856eecf7a105eed86227d05717be29f`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/LifecycleRecoveryAdapter.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/LifecycleRecoveryAdapterTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-11.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-11/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-11/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/LifecycleRecoveryAdapter.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-11.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-11` · primary · Day 151 · 13 Mar 2027 — Display bounded recovery and reconciliation outcomes (150m)

Description SHA-256: `6bba100cd2ceeafd76b1d818b0f5cd36a856eecf7a105eed86227d05717be29f`

**Practical task:** CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

Exact source description: [saved curriculum record `CAP-11`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Connect the confirmed recovery UI to server-defined safe work eligibility and honest asynchronous state, preserving effect identity across attempts.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/LifecycleRecoveryAdapter.java
- src/test/java/capstone/coverstory/application/LifecycleRecoveryAdapterTest.java
- docs/coverstory/fixtures/cap-11.json
- docs/evidence/coverstory/CAP-11/result.json
- docs/evidence/coverstory/CAP-11/explain-back.md
- frontend/src/coverstory/application/LifecycleRecoveryAdapter.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-11.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named LifecycleRecoveryAdapter TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Connect the confirmed recovery UI to server-defined safe work eligibility and honest asynchronous state, preserving effect identity across attempts.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Eligible unsent/recoverable simulator job, exhausted retry budget, unknown insurer outcome, conflicting recovery command and permission denial.

Output: One authorized local recovery request or explicit reconciliation/help; refreshed authoritative history, no fabricated attempt or reset budget.

#### Explicit acceptance cases — expected, not observed

CAP-11-CS-1
Given: Eligible failed-before-send work is confirmed
Expected: Show recovery pending with same logical effect reference and refresh authoritative status.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-11-CS-2
Given: Outcome UNKNOWN or retry exhausted without authorized eligibility
Expected: No blind resubmit/cancel/refund; show reconciliation or bounded support action.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-11-CS-3
Given: Conflict indicates another recovery already began
Expected: Refresh authoritative application/attempts; do not append fake success or allocate another money effect.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: recovery-versus-reconciliation state machine at planned docs/architecture/coverstory/CAP-11-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): ServerEligibility, ConfirmedRecoveryCommand, PendingRecovery, AuthoritativeHistory, UnknownExternalOutcome, SupportAction.

Change or annotate only these facts (maximum 3):
- Only known eligible work may retry the same logical mock effect.
- Unknown external outcome takes the reconciliation branch.
- Conflict refreshes authoritative history instead of appending a fake attempt.

Alternative to compare: Reset exhausted budgets or replay any unknown job from the UI.

Interview prompt: Why is failure-before-send different from timeout-after-possible-acceptance when offering the recovery button?

Failure evidence: CAP-11-CS-2 and CAP-11-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-10](https://dwait-to-adwait.youtrack.cloud/issue/CAP-10) — Day 150; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
