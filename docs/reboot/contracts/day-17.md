# Day 17 — 2026-11-05: Render product choices in React/TypeScript

**State:** Minimal React/TypeScript workspace, fixed component tests, and learner TODO are installed. The UI workspace builds, while all component behavior tests currently fail at the placeholder.

**Course lesson:** Conditional reference to [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux/), Academind by Maximilian Schwarzmüller; course access is not confirmed. “React Essentials - Components, JSX, Props, State & More”: “Making Components Reusable with Props [Core Concept]” (9:06). “React + TypeScript”: “Working with Props & TypeScript” (14:20). Total 23:26. Use these for typed props only; use the linked project example for discriminated UI-state behavior. No purchase/enrollment is requested. See the [Days 1–30 course map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-17---typed-props-and-ui-state).

## Learner seam

`frontend/src/components/ProductPanel.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Typed fixture renders labels/options; loading/error/empty explicit; accessible names; no hard-coded production URL. Test adapter avoids dependency on earlier live backend.

## Required edge cases

Loading, error, empty, product options, accessibility query, no real fetch.

## Prerequisite and isolation

Day 4 contract; canned fetch adapter. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Sketch the UI state and API boundary needed for product choices; keep server policy authoritative. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Typed props and discriminated UI state [K,L].

## Evidence (10 minutes)

Component tests and screenshot. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`npm --prefix frontend test -- src/components/ProductPanel.test.tsx`

Run this focused component suite; its four failures are expected at the Day 17 TODO. `npm --prefix frontend run build` verifies TypeScript/bundling separately. The Vite app is only a test scaffold; it does not claim a finished UI.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 3 Longest Substring Without Repeating Characters — variable sliding window. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Which UI states exist before and after a request? Why do types not replace runtime checks?

**Observable evidence:** Show loading/error/empty/content behavior, accessible labels and typed boundaries; explain state ownership.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
