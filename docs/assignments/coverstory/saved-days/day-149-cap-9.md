# Day 149 · 11 Mar 2027 — Render source-dated application and refund timelines (150m)

**Date:** 2027-03-11 · **Phase:** 1 · **Primary ID:** `CAP-9` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `ea1145017f7d596c17ab88754d4ae1b375fe4c3eb2a247b9f2b1a2a814e5f518`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/LifecycleTimeline.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/LifecycleTimelineTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-9.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-9/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-9/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/LifecycleTimeline.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-9.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-9` · primary · Day 149 · 11 Mar 2027 — Render source-dated application and refund timelines (150m)

Description SHA-256: `ea1145017f7d596c17ab88754d4ae1b375fe4c3eb2a247b9f2b1a2a814e5f518`

**Practical task:** CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

Exact source description: [saved curriculum record `CAP-9`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Render the authoritative timeline query through shared typed components using immutable server sequence and domain references. Preserve UNKNOWN/reconciled observations; keep best-effort analytics outside the timeline.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/LifecycleTimeline.java
- src/test/java/capstone/coverstory/application/LifecycleTimelineTest.java
- docs/coverstory/fixtures/cap-9.json
- docs/evidence/coverstory/CAP-9/result.json
- docs/evidence/coverstory/CAP-9/explain-back.md
- frontend/src/coverstory/application/LifecycleTimeline.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-9.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named LifecycleTimeline TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Render the authoritative timeline query through shared typed components using immutable server sequence and domain references. Preserve UNKNOWN/reconciled observations; keep best-effort analytics outside the timeline.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Unordered payment/onboarding/submission/review/offer/refund/issuance events, equal timestamps, empty history and unknown external outcome.

Output: Accessible chronological timeline with actor/source and no fabricated event or inferred coverage.

#### Explicit acceptance cases — expected, not observed

CAP-9-CS-1
Given: Events return out of order with stable aggregate versions
Expected: Render once in version/id order with matching occurredAt and receivedAt, retaining late stale evidence labels.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-9-CS-2
Given: Submission is pending and no dispatch evidence exists
Expected: Show no confirmed insurer submission yet; do not invent successful attempt.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-9-CS-3
Given: An UNKNOWN payment later reconciles, timestamps tie, or click events arrive out of order
Expected: Retain both business observations in server sequence, link the one actual effect, and never infer progress from analytics or elapsed time.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: authoritative timeline projection ER/data-flow sketch at planned docs/architecture/coverstory/CAP-9-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): DomainEffect, TransactionAuditEvent, JourneyStepEvent, TimelineQuery, TypedTimelineView, AnalyticsEvent.

Change or annotate only these facts (maximum 3):
- Stable server sequence orders observations, not client timestamps.
- UNKNOWN and later reconciliation remain separate historical facts.
- The view counts unique actual effects and excludes telemetry as authority.

Alternative to compare: Replace an earlier UNKNOWN row with success or sort only by timestamps.

Interview prompt: Two observations share a timestamp and one click arrives late. How does the UI retain an honest order without inventing a successful attempt?

Failure evidence: CAP-9-CS-2 and CAP-9-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-8](https://dwait-to-adwait.youtrack.cloud/issue/CAP-8) — Day 148; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
