# Day 53 — 2026-12-15: Add accessible loading and unavailable states

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPanel`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Loading is role=status; unauthenticated/unavailable are role=alert. Ready without a selected preview shows a safe unavailable alert. Non-ready state clears stale details. Changed ready data clears old alert. Refresh exists only when supplied and runs once on click, never during render/rerender.

### Readiness before the task

- Day52 ready display and earlier state/Promise teaching

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-53.json` only after a real learner attempt.

**Acceptance commands:**
- `npm --prefix frontend run test:learner -- src/__tests__/PolicyRenewalPreviewPanel.test.tsx -t "explicit|refresh"`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.
## Practice and evidence

**85-minute practice allocation:** 5 minutes to save the task-specific prediction before assistance; the remaining 80 minutes are for the unchanged acceptance command(s) above, stopping at the first unresolved prerequisite or timebox. Preserve any unfinished criteria as open work; no overtime.

**10-minute evidence allocation:** record the actual command/output, source SHA, result or first blocker, changed paths, and assistance. Do not infer learner completion from a green instructor reference or write a post-hoc architecture justification.

## Study block — exactly 25 minutes

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-53](<../../assistant-architecture-notes-15-70.md#day-53>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

- Required source: **Handling Loading States**, section **Sending Http Requests (e.g. Connecting to a Database)** (5:27), access `unconfirmed_reference_only`. No viewing is inferred.
- Worked guide: [Keep states exclusive and refresh explicit](../worked/day-53-accessible-read-refresh.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [W3C WCAG technique ARIA22: Using role=status to present status messages](https://www.w3.org/WAI/WCAG22/Techniques/aria/ARIA22) — Description; Examples.
- [W3C APG: Alert Pattern](https://www.w3.org/WAI/ARIA/apg/patterns/alert/) — About This Pattern; Keyboard Interaction.
- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure#avoid-contradictions-in-state) — Avoid contradictions in state.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required. Optional lesson references are deferred recovery only and do not add time.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
