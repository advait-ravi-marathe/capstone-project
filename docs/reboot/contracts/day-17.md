# Day 17 — 2026-11-05: Render product choices in React/TypeScript

**State:** Minimal React/TypeScript workspace, fixed component tests, and learner TODO are installed. The UI workspace builds, while all component behavior tests currently fail at the placeholder.

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

## Interview transfer (inside the current evidence block)

**Question:** Which UI states exist before and after a request? Why do types not replace runtime checks?

**Observable evidence:** Show loading/error/empty/content behavior, accessible labels and typed boundaries; explain state ownership.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-11-05 --roadmap-day 17` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
