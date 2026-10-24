# Day 19 — 2026-11-11: Display quote snapshot

**State:** React/TypeScript component scaffold and fixed acceptance tests are installed; learner implementation remains pending. The focused suite is expected to fail at unimplemented rendering/interaction behavior.

**Coursework before implementation:** If available, use course 1362070 “Using async / await” (2:25) and “Handling Loading States” (5:27), 7:52 total; access is unconfirmed. TanStack query-key/cancellation videos are not assigned. First use the [Day 19 deferred-refresh guide](../teaching/day-19-latest-refresh.md#worked-trace) to trace stale success, stale error, and stale finally behavior. No new dependency; total study 25 minutes.

## Learner seam

`QuoteSummary.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Show server `annualPremiumRupees`, currency, `productVersion`, `rateVersion`, and `expiresAt` exactly; browser never recomputes price. Loading/error/refresh are explicit; latest request wins when responses arrive out of order.

## Required edge cases

Exact decimal text, error/loading, deferred promises resolved out of order.

## Prerequisite and isolation

Day 7 response contract; controlled deferred promises. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Teach-first design question (within the current concept block):** If A starts before B but finishes after B, which may update data, error, and loading? Why is an obsolete finally block still a state write? Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Async UI, race conditions and state ownership [K,L].

## Evidence (10 minutes)

Out-of-order response component test. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day19Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Two requests finish out of order. Which response should win and why?

**Observable evidence:** Predict the controlled-promise test; explain stale-response suppression and server-owned quoted price.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
