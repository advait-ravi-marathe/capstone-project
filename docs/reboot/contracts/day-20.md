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

**Teach-first design question (within the current concept block):** When does the UI show success for Promise<void>? What should 409, 401, 404, and an unknown transport failure communicate? Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Use the [Day 20 mutation guide](../teaching/day-20-truthful-mutation.md#worked-trace) before implementation. It teaches the exact Promise<void> status boundary, pending guard, safe messages, and why a transport failure may leave outcome unknown.

## Interview-first proposal (5 minutes inside practice)

Before implementation hints or AI, write a brief task-specific proposal: the user-visible result, data/UI boundary, one expected case, and one failure case. Save it, then continue the original acceptance in the remaining 80 minutes of the existing practice block. Do not add a post-hoc design essay. The 10-minute evidence block records actual command, result, assistance, changed paths, and blocker; unfinished acceptance stays pending in the normal recovery queue.

## Evidence (10 minutes)

Four component scenarios. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day20Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
