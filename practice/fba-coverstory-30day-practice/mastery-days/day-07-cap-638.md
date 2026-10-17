# Day 07 · 2028-06-24 · CAP-638 / CSM14-07

**Board contract:** [CAP-638](https://dwait-to-adwait.youtrack.cloud/issue/CAP-638) — CSM14-07 · 24 Jun 2028 — Render explicit branch conflicts in React (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-05, CSM14-06, CAP-18, CAP-19

## Contract focus

Implement one bounded React composition in the existing comparison screen using shared primitives and existing TanStack Query factories. Render the base/local/upstream source versions and one explicit decision control for each of at most three conflicts. Server preview lives in the query cache; unsaved choices remain local memory state tied to the preview fingerprint. The accept control submits the exact resolved plan and is disabled while unresolved, stale or unavailable.

## Synthetic fixture

Prepared API fixtures return one auto-resolved slot, one divergent-version conflict and one unavailable slot. Resolve the conflict, then receive a changed preview fingerprint. Exercise keyboard-only choice, loading, visible error and retry through the existing shell. No new route framework or design system is introduced.

## Acceptance checklist

- [ ] Visible source/version labels distinguish base, local intent and upstream proposal without implying that the proposal has already been saved.
- [ ] Unresolved/unavailable/stale plans cannot invoke acceptance; tests observe zero mutation calls.
- [ ] A changed preview invalidates incompatible local decisions and asks for explicit review; decisions never silently attach to another version pair.
- [ ] Keyboard focus and accessible names allow selection and error recovery with the existing shared controls.
- [ ] An API failure retains the unsaved draft in memory while clearly distinguishing it from the current persisted checkpoint.

## Architecture/evidence

Record a small state-ownership table and one executable state-transition trace. Explain which facts are authoritative server state, which are local draft intent, and how their lifetimes differ.

## Scope and provenance

This new UI requires explicit three-way conflict resolution; existing saved-selection, shared-component and query-race foundations are reused.

Structured contract hash: `37e362172550ef4411c907ba9776463f28e03a4de6e3a065f405b44e62f24f52`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap638_conflictUiDisablesAcceptUntilAccessibleExplicitDecision test`
Browser behavior check: `node web/scripts/csm14-learner-acceptance.mjs CAP-638` (checks blocked unavailable/stale states, fingerprint change, and an explicit usable resolution; this subset passes on the starter. Full choice-to-source fingerprint binding and the integrated API flow remain learner work.)
