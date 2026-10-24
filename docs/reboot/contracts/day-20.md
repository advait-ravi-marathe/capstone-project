# Day 20 — 2026-11-12: Connect selection action UI

**State:** React/TypeScript component scaffold and fixed acceptance tests are installed; learner implementation remains pending. The focused suite is expected to fail at unimplemented rendering/interaction behavior.

**Coursework before implementation:** If available, use course 1362070 “Handling HTTP Errors” (9:16); access is unconfirmed. Backend POST/201 lessons are not assigned for this Promise<void> UI contract. First use the [Day 20 truthful mutation guide](../teaching/day-20-truthful-mutation.md#worked-trace) for the exact injected function, pending guard, status-to-message map, and ambiguous transport failure. No purchase/enrollment; total study 25 minutes.

## Learner seam

`SelectQuoteButton.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

POST exact journey/version once while pending. HTTP 200 is server-confirmed selection; 409 prompts refresh, 401 prompts sign-in, 404 shows unavailable. Never imply purchase or active coverage.

## Required edge cases

Success, conflict, auth, unavailable, network error, double click.

## Prerequisite and isolation

Days 1, 3 and 11 response contracts; fake API. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-20](<../assistant-architecture-notes-15-70.md#day-20>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Use the [Day 20 mutation guide](../teaching/day-20-truthful-mutation.md#worked-trace) before implementation. It teaches the exact Promise<void> status boundary, pending guard, safe messages, and why a transport failure may leave outcome unknown.

## Evidence (10 minutes)

Four component scenarios. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day20Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
