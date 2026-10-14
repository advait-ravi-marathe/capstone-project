# Day 142 · 4 Mar 2027 — Refine typed UI primitives, tokens and responsive themes (150m)

**Date:** 2027-03-04 · **Phase:** 1 · **Primary ID:** `CAP-3` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `1c06860dff98f0b40c39e5388aeab51f4d7b9b109dc34d778a8f07204cdbeb2f`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Refine shared Button, Field, StatusBadge and JourneyCard compositions with finite typed variants and Tailwind tokens; apply the open-source Inter typography decision across existing mobile/desktop lifecycle screens.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/LifecycleResponsiveLayout.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/LifecycleResponsiveLayoutTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-3.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-3/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-3/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/LifecycleResponsiveLayout.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-3.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-3` · primary · Day 142 · 4 Mar 2027 — Refine typed UI primitives, tokens and responsive themes (150m)

Description SHA-256: `1c06860dff98f0b40c39e5388aeab51f4d7b9b109dc34d778a8f07204cdbeb2f`

**Practical task:** Refine shared Button, Field, StatusBadge and JourneyCard compositions with finite typed variants and Tailwind tokens; apply the open-source Inter typography decision across existing mobile/desktop lifecycle screens.

Exact source description: [saved curriculum record `CAP-3`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Refine shared Button, Field, StatusBadge and JourneyCard compositions with finite typed variants and Tailwind tokens; apply the open-source Inter typography decision across existing mobile/desktop lifecycle screens.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/LifecycleResponsiveLayout.java
- src/test/java/capstone/coverstory/application/LifecycleResponsiveLayoutTest.java
- docs/coverstory/fixtures/cap-3.json
- docs/evidence/coverstory/CAP-3/result.json
- docs/evidence/coverstory/CAP-3/explain-back.md
- frontend/src/coverstory/application/LifecycleResponsiveLayout.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-3.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named LifecycleResponsiveLayout TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Refine shared Button, Field, StatusBadge and JourneyCard compositions with finite typed variants and Tailwind tokens; apply the open-source Inter typography decision across existing mobile/desktop lifecycle screens.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Prepared shared primitives and typed variant map, Tailwind tokens, Inter font/license assets, 390/1440px views, 200% zoom, keyboard navigation, long offer text, light/dark and font/storage-failure fixtures.

Output: Responsive CSS/component refinement plus accessible captures and no product-state mutation.

#### Explicit acceptance cases — expected, not observed

CAP-3-CS-1
Given: Two customer screens use the same declared Button/Field/StatusBadge variants at 390px and 200% zoom
Expected: One reusable primitive renders consistent reachable controls and readable labels; no per-screen arbitrary class-string or duplicated business state.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-3-CS-2
Given: Keyboard and dark/light controls are exercised
Expected: Visible focus, non-color error text, source contrast checks and labelled theme state; no inaccessible paid-option selection.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-3-CS-3
Given: Font or theme persistence fails, or an unknown component variant is requested
Expected: Readable system-font fallback and safe theme behavior; unknown typed variant is rejected by checks. No change to quote, answers, consent or money; include Inter license notice when assets are prepared.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: shared UI composition and token graph at planned docs/architecture/coverstory/CAP-3-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): TailwindTokens, InterFontToken, FiniteVariantMap, ButtonAndField, JourneyCard, TwoDomainScreens.

Change or annotate only these facts (maximum 3):
- Complete static utility variants are shared and included in the production build.
- Typography hierarchy is independent of semantic heading level.
- Responsive/font/theme failures preserve readable accessible controls.

Alternative to compare: Interpolate dynamic class fragments and duplicate arbitrary styles in each screen.

Interview prompt: Why can a variant work during development but disappear from built CSS, and how do the two consuming screens prove shared behavior?

Failure evidence: CAP-3-CS-2 and CAP-3-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-2](https://dwait-to-adwait.youtrack.cloud/issue/CAP-2) — Day 141; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
