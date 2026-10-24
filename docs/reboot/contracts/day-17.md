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

**Teach-first design question (within the current concept block):** How does the state union narrow after each early return? Which accessible role/name identifies Hindi, and where is its code stored? Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Use the [rendered Day 17 guide](../teaching/day-17-render-typed-state.md#worked-trace) before implementing the component. It demonstrates all four state branches and role/name queries without a fetch or production URL.

## Evidence (10 minutes)

## Planned isolated command

`npm --prefix frontend test -- src/components/ProductPanel.test.tsx`

Run this focused component suite; its four failures are expected at the Day 17 TODO. `npm --prefix frontend run build` verifies TypeScript/bundling separately. The Vite app is only a test scaffold; it does not claim a finished UI.

## Interview transfer (inside the current evidence block)

**Question:** Which UI states exist before and after a request? Why do types not replace runtime checks?

**Observable evidence:** Show loading/error/empty/content behavior, accessible labels and typed boundaries; explain state ownership.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
