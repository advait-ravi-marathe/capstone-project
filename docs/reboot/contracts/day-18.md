# Day 18 — 2026-11-10: Capture quote input

**State:** React/TypeScript component scaffold and fixed acceptance tests are installed; learner implementation remains pending. The focused suite is expected to fail at unimplemented rendering/interaction behavior.

**Coursework before implementation:** If available, use course 1362070 “Managing & Getting User Input via State & Generic Handlers” (11:12); access is unconfirmed. “User Input & Two-Way-Binding” (7:42) is optional replacement-only. First use the [Day 18 controlled form and Promise guide](../teaching/day-18-await-form-promise.md#worked-trace), including success, rejection, pending, and duplicate-submit evidence. No purchase/enrollment; total study 25 minutes.

## Learner seam

`QuoteForm.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Required inputs are occupancy, rebuild amount in INR and one Day 4 coverage code; each has an accessible label. Valid synthetic data submits once; invalid input has field feedback; pending blocks duplicate submit. Server remains authoritative and repeats Day 5 bounds only for feedback.

## Required edge cases

Keyboard submit, invalid input, pending double click, success and rejection.

## Prerequisite and isolation

Day 5 request contract; fake submit promise. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Teach-first design question (within the current concept block):** What is the call count while the first save is pending? Why must pending release after both fulfillment and rejection, while the input remains available? Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Controlled inputs, derived state, client/server validation [K].

## Evidence (10 minutes)

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day18Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Can client validation enforce a business rule? What prevents double submission?

**Observable evidence:** Demonstrate invalid/pending/keyboard cases; explain server authority and why a disabled button alone cannot protect the API.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal); [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
