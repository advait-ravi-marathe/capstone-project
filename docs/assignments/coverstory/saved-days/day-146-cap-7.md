# Day 146 · 8 Mar 2027 — Test duplicate proposal-submit interactions (150m)

**Date:** 2027-03-08 · **Phase:** 1 · **Primary ID:** `CAP-7` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `1f4f8426d26fa307f839d5f196109fe71fd8b86d30c30b5c12dce7009d100c84`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/ProposalSubmitInteractionTest.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/ProposalSubmitInteractionTestTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-7.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-7/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-7/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/ProposalSubmitInteractionTest.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-7.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-7` · primary · Day 146 · 8 Mar 2027 — Test duplicate proposal-submit interactions (150m)

Description SHA-256: `1f4f8426d26fa307f839d5f196109fe71fd8b86d30c30b5c12dce7009d100c84`

**Practical task:** CoverStory is a proposed health-first insurance marketplace teaching application, with entry points for health, term life, motor and ULIP. This card deepens the cumulative target journey; it does not claim that target is implemented today. The current repository audit reports only a Spring /health endpoint under capstone.app, fixture-mode React, and reusable generic AI/evaluation mechanisms. The six product modules, public interfaces, private persistence, component scanning and boundary tests are planned instructor prerequisites. Existing OperatorApi/EventStatus are historical contracts, never automatic insurance aliases.

Exact source description: [saved curriculum record `CAP-7`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Test duplicate interaction, mutation response loss and deliberate authoritative query invalidation without relying on button disabling or optimistic financial status for idempotency.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/ProposalSubmitInteractionTest.java
- src/test/java/capstone/coverstory/application/ProposalSubmitInteractionTestTest.java
- docs/coverstory/fixtures/cap-7.json
- docs/evidence/coverstory/CAP-7/result.json
- docs/evidence/coverstory/CAP-7/explain-back.md
- frontend/src/coverstory/application/ProposalSubmitInteractionTest.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-7.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named ProposalSubmitInteractionTest TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Test duplicate interaction, mutation response loss and deliberate authoritative query invalidation without relying on button disabling or optimistic financial status for idempotency.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Same actor/resource/key/payload, response loss after durable queue commit, same key with changed revision/hash, deliberate new application.

Output: Interaction counts, stable submission reference and one logical dispatch allocation.

#### Explicit acceptance cases — expected, not observed

CAP-7-CS-1
Given: Two clicks occur before first response
Expected: At most one in-flight UI request and one durable submission job.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-7-CS-2
Given: The command commits domain/audit/journey changes but the response is lost, then identical request repeats
Expected: Return the same operation reference and one authoritative journey-step event; money remains counted once. Analytics duplicates have separate receipt semantics.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-7-CS-3
Given: Key reused with changed reviewed revision/hash
Expected: Explicit conflict; original application/submission evidence unchanged.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: UI interaction versus durable idempotency graph at planned docs/architecture/coverstory/CAP-7-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): SubmitButton, MutationState, IdempotencyIdentity, StoredCommandResult, JourneyStepUniqueness, QueryInvalidation.

Change or annotate only these facts (maximum 3):
- In-flight suppression and durable dedup solve different failures.
- Same key and exact payload recovers the same operation.
- Server-confirmed updates narrowly invalidate affected queries without automatic new writes.

Alternative to compare: Use button disabling as the only duplicate defense or retry every mutation blindly.

Interview prompt: Why can two network requests still produce one business action, while a changed payload under the same key must conflict?

Failure evidence: CAP-7-CS-2 and CAP-7-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-6](https://dwait-to-adwait.youtrack.cloud/issue/CAP-6) — Day 145; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
