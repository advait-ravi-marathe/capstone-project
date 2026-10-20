# Day 18 — 2026-11-06: Capture quote input

**State:** React/TypeScript component scaffold and fixed acceptance tests are installed; learner implementation remains pending. The focused suite is expected to fail at unimplemented rendering/interaction behavior.

## Learner seam

`QuoteForm.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Required inputs are occupancy, rebuild amount in INR and one Day 4 coverage code; each has an accessible label. Valid synthetic data submits once; invalid input has field feedback; pending blocks duplicate submit. Server remains authoritative and repeats Day 5 bounds only for feedback.

## Required edge cases

Keyboard submit, invalid input, pending double click, success and rejection.

## Prerequisite and isolation

Day 5 request contract; fake submit promise. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show user-entered fields flowing to the quote request and identify validation ownership. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Controlled inputs, derived state, client/server validation [K].

## Evidence (10 minutes)

Keyboard, validation and pending-state component tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day18Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 125 (D+7 from Day 11), LC 11 (D+3 from Day 15). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Can client validation enforce a business rule? What prevents double submission?

**Observable evidence:** Demonstrate invalid/pending/keyboard cases; explain server authority and why a disabled button alone cannot protect the API.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A6; A5](../interview-evidence-overlay.md#a6; a5) (anecdotal). These sources motivate practice only; they do not establish question frequency or predict an interview.
