# Day 145 · 7 Mar 2027 — Deepen the exact proposal-review submission form (150m)

**Date:** 2027-03-07 · **Phase:** 1 · **Primary ID:** `CAP-6` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `cff5bfffc851e7140a77670b072283e5b6933bbdaa6ea1545c80aa3da3a1fc6e`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Refine the existing exact-review form using an explicit mutation boundary, with local draft state separate from TanStack server state and all server submission gates intact.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/internal/ProposalReviewForm.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/ProposalReviewFormTest.java` (planned in source; existence/readiness is not implied)
- `docs/coverstory/fixtures/cap-6.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-6/result.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-6/explain-back.md` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/ProposalReviewForm.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/query/queryClient.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/application/query/journeyKeys.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/session/SessionBootstrap.tsx` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/ui/variants.ts` (planned in source; existence/readiness is not implied)
- `frontend/src/coverstory/shared/theme/tokens.css` (planned in source; existence/readiness is not implied)
- `docs/architecture/coverstory/frontend-CAP-6.md` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-6` · primary · Day 145 · 7 Mar 2027 — Deepen the exact proposal-review submission form (150m)

Description SHA-256: `cff5bfffc851e7140a77670b072283e5b6933bbdaa6ea1545c80aa3da3a1fc6e`

**Practical task:** Refine the existing exact-review form using an explicit mutation boundary, with local draft state separate from TanStack server state and all server submission gates intact.

Exact source description: [saved curriculum record `CAP-6`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Daily outcome

Refine the existing exact-review form using an explicit mutation boundary, with local draft state separate from TanStack server state and all server submission gates intact.

#### Instructor scaffold — planned paths and prerequisites

- src/main/java/capstone/coverstory/application/internal/ProposalReviewForm.java
- src/test/java/capstone/coverstory/application/ProposalReviewFormTest.java
- docs/coverstory/fixtures/cap-6.json
- docs/evidence/coverstory/CAP-6/result.json
- docs/evidence/coverstory/CAP-6/explain-back.md
- frontend/src/coverstory/application/ProposalReviewForm.tsx
- frontend/src/coverstory/shared/query/queryClient.ts
- frontend/src/coverstory/application/query/journeyKeys.ts
- frontend/src/coverstory/session/SessionBootstrap.tsx
- frontend/src/coverstory/shared/ui/variants.ts
- frontend/src/coverstory/shared/theme/tokens.css
- docs/architecture/coverstory/frontend-CAP-6.md

Public interface: ApplicationCommandApi; Application/Payments facts are read through their owning public module APIs. The model receives read-only Support evidence ports only.

- Reconcile package scanning with current capstone.app and explicitly expose all six product modules through public interfaces; no cross-module repositories.
- Supply build configuration, empty seam/signature, response and persistence types, deterministic fake external adapters, resettable synthetic data and fixed visible tests without learner implementation.
- Supply only the declared mechanical test/server/render wiring. Any missing whole product transition or transaction is a named BLOCKED prerequisite; never hide it as assumed glue.
- Prepare database transaction barriers or fake clock as this card requires, plus zero-write/ownership observation and an exact evidence command.
- Mark reused generic mechanisms separately from new insurance interfaces; old OperatorApi/EventStatus and signed-event/replay contracts remain archived.

Proposed registry: docs/coverstory/fixtures/synthetic-registry-v1.json. Reconcile the source fixture aliases, integer INR units and immutable source/terms/configuration hashes before preparation. Product snapshots remain independent from archived generic fixture IDs.

#### Learner TODO

- Predict the three fixed outcomes, then complete only the named ProposalReviewForm TODO(s) against the supplied public module interface. Do not implement an unprepared whole workflow during the learner slot.
- Refine the existing exact-review form using an explicit mutation boundary, with local draft state separate from TanStack server state and all server submission gates intact.
- Run the fixed positive/failure/boundary cases and one small changed-input case whose expected result is written before implementation; preserve every historical conceptual invariant identified by this migration.
- Record exact commit/environment, fixture and source versions, command, expected/actual, PASS/FAIL/BLOCKED/NOT_RUN and one explained correction. Leave actual results blank until observed.

#### Concrete inputs and outputs

Input: Owned application r3/current hash, accepted quote/options configuration, complete disclosures, confirmed net and no pending adjustment; stale and 503 variants.

Output: One deliberate command with expectedVersion, reviewedRevision, quoteId, exact terms/configuration identity and consent; truthful SUBMISSION_PENDING response.

#### Explicit acceptance cases — expected, not observed

CAP-6-CS-1
Given: Valid current review returns 202 SUBMISSION_PENDING
Expected: Show queued proposal with reference and no-cover-yet, never Submitted or Issued until evidence.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-6-CS-2
Given: Missing consent, stale revision, unresolved field or changed amount/terms even by 1 paise
Expected: No client progression; field/review error; server independently rejects stale or unreconciled command.
Actual: NOT_RUN; record the exact observed output after execution.

CAP-6-CS-3
Given: SQL fails between domain, transaction audit, journey projection and step-event writes, or response is lost after commit
Expected: Failure rolls all related state back; lost response recovers the same committed operation reference. No partial advance, duplicate effect or analytics-derived success.
Actual: NOT_RUN; record the exact observed output after execution.

#### Architecture and interview delta

Artifact: exact-review mutation and atomic state sequence at planned docs/architecture/coverstory/CAP-6-delta.md. Status: PROPOSED / NOT_RUN.

Named nodes (maximum 6): LocalReviewDraft, MutationCommand, DomainExpectedVersion, AuditJourneyTransaction, AuthoritativeGET, ResumeView.

Change or annotate only these facts (maximum 3):
- The command binds exact revision/quote/configuration/terms and explicit consent.
- Domain, required audit, journey and step writes share one unit of work.
- Unknown response recovers by same operation reference or GET, never optimistic issued state.

Alternative to compare: Mark the journey submitted when the button is clicked and persist audit later.

Interview prompt: Failure occurs between audit and journey writes. What must roll back, and what changes if the response is lost only after commit?

Failure evidence: CAP-6-CS-2 and CAP-6-CS-3. One annotated bounded artifact and a short failure-linked explain-back. Mark actual observations NOT_RUN until tested; no supplied learner solution or assessed score.

Provide a small partially filled artifact template with the named nodes and blank links/changed facts. Learner changes at most three facts; missing implementation stays explicit rather than being drawn as working. Use at most 5 minutes already inside capstone docs/tests-evidence and at most 3 minutes of the existing reflection slot; Sundays use the existing individual-evidence slot, never DSM. No extra block or evening work.

#### Earlier CAP evidence dependencies

- [CAP-164](https://dwait-to-adwait.youtrack.cloud/issue/CAP-164) — Day 138; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
- [CAP-5](https://dwait-to-adwait.youtrack.cloud/issue/CAP-5) — Day 144; The prior product seam and its named fixed tests must actually be prepared and observed. A planned date or old generic test pass is not prerequisite completion.
