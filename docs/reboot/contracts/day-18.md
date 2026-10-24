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

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-18](<../assistant-architecture-notes-15-70.md#day-18>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Controlled inputs, derived state, client/server validation [K].

## Evidence (10 minutes)

Keyboard, validation and pending-state component tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day18Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
