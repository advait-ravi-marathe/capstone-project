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

## Evidence (10 minutes)

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day20Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Should a timeout be retried blindly? How do 401 and 409 alter UI behavior?

**Observable evidence:** Trace success/conflict/unknown-outcome states; avoid claiming coverage was purchased; distinguish transport retry from safe domain repetition.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
