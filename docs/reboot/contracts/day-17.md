# Day 17 — 2026-11-09: Render product choices in React/TypeScript

**State:** Minimal React/TypeScript workspace, fixed component tests, and learner TODO are installed. The UI workspace builds, while all component behavior tests currently fail at the placeholder.

**Coursework before implementation:** If available, use course 1362070 “Making Components Reusable with Props [Core Concept]” (9:06); access is unconfirmed. “Working with Props & TypeScript” (14:20) is optional replacement-only. First use the [Day 17 rendered-state guide](../teaching/day-17-render-typed-state.md#worked-trace) for all four states, accessible role/name queries, and a distinct variation. No purchase/enrollment; total study 25 minutes.

## Learner seam

`frontend/src/components/ProductPanel.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Typed fixture renders labels/options; loading/error/empty explicit; accessible names; no hard-coded production URL. Test adapter avoids dependency on earlier live backend.

## Required edge cases

Loading, error, empty, product options, accessibility query, no real fetch.

## Prerequisite and isolation

Day 4 contract; canned fetch adapter. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-17](<../assistant-architecture-notes-15-70.md#day-17>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Use the [rendered Day 17 guide](../teaching/day-17-render-typed-state.md#worked-trace) before implementing the component. It demonstrates all four state branches and role/name queries without a fetch or production URL.

## Interview-first system-design mock (45 minutes inside practice)

**Prompt:** [Q01](../interview/question-bank.md#q01) — Foundations URL shortener. For this interview question only, write and save your initial proposal before hints, reference answers, or AI. Use the exact 5/5/15/10/10 phases and 100-point rubric in [the mock rubric](../interview/mock-rubric.md).

**Timebox:** 45 minutes of the existing 85-minute practice block for the mock, then 40 minutes for this EAI slice: Render the ready-state product and accessible coverage options from the supplied typed fixture; leave the remaining states in the named queue. No overtime. The unchanged full acceptance selector remains the completion gate.

**Deferred acceptance:** `S2-D17-1` is in the [Sprint 2 session scope](../interview/sprint2-session-scope.md). Preserve the actual result and keep this day pending until the named completion check and all original acceptance criteria pass.

## Evidence (10 minutes)

Component tests and screenshot. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`npm --prefix frontend test -- src/components/ProductPanel.test.tsx`

Run this focused component suite; its four failures are expected at the Day 17 TODO. `npm --prefix frontend run build` verifies TypeScript/bundling separately. The Vite app is only a test scaffold; it does not claim a finished UI.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
