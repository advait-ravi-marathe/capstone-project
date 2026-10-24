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

## Interview-first proposal (5 minutes inside practice)

Before implementation hints or AI, write a brief task-specific proposal: the user-visible result, data/UI boundary, one expected case, and one failure case. Save it, then continue the original acceptance in the remaining 80 minutes of the existing practice block. Do not add a post-hoc design essay. The 10-minute evidence block records actual command, result, assistance, changed paths, and blocker; unfinished acceptance stays pending in the normal recovery queue.

## Evidence (10 minutes)

Keyboard, validation and pending-state component tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day18Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
