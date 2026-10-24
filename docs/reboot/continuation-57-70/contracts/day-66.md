# Day 66 — 2026-12-28: Build the normal React route and typed local API client

## Contract

**Learner-owned seam:** `LocalCoverStoryApp and localPolicyApi`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The packaged root route `/` uses a typed same-origin client for demo-session selection, renewal preview, policy issue, policy readback and same-key replay. It presents loading, ready, unavailable and expired-session states accessibly; it contains no `/test/**` or `/__e2e/**` route.

## Acceptance and expected TODO state

**Acceptance harness:** learner-owned bounded contract

The Sprint5-only selector covers original UI completion criteria plus malformed success, HTML 401/403/500, network failure, pending action, no auto-retry, CSRF-protected bodyless logout, owner clearing, and late old-owner result rejection. Later busy/capacity tests remain separately selected by `test:local`.

**Acceptance selector:** `the six explicitly named tests in test:sprint5`
**Acceptance source:** `frontend/src/__tests__/LocalCoverStoryApp.test.tsx`
**Command:**

```sh
npm --prefix frontend run test:sprint5
```

The selector is intentionally expected to fail at this day’s unchanged learner TODO until implementation evidence exists. This prospective harness is not completion credit.

## Interview design mock (45 minutes inside the 85-minute practice block)

**Question:** [Q14 — Booking: retry after a lost response](../../interview/question-bank.md#q14)

Design a retry after a booking write succeeds but its response is lost. Distinguish the frozen user intent and idempotency key from transport retries, define visible pending/unknown/confirmed states, and explain why a retry must reuse the same request. Transfer to the existing local UI; do not add booking behavior.

For this interview question only, save the initial proposal before opening hints, worked references, or AI. Use Requirements 5 minutes; Capacity and constraints 5; Initial architecture and data model 15; Bottlenecks, reliability, scaling 10; Trade-offs, failures, follow-ups 10. Use the [100-point rubric](../../interview/mock-rubric.md).

**Remaining task slice (40 minutes):**

- 5 minutes: preconditions and prediction.
- 25 minutes: Implement one coherent learner-owned typed same-origin session-plus-preview vertical slice with explicit response decoding and owner/session state clearing. Use only existing API shapes; no issue POST from a React effect and no new retry mechanism.
- 10 minutes: run the named selector and record its result or first blocker. The full day acceptance above remains unchanged; unmet work stays open as **S5-D66-UI**.

## Selector limits and open evidence

The Sprint5-only selector covers original UI completion criteria plus malformed success, HTML 401/403/500, network failure, pending action, no auto-retry, CSRF-protected bodyless logout, owner clearing, and late old-owner result rejection. Later busy/capacity tests remain separately selected by `test:local`. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-66](<../../assistant-architecture-notes-15-70.md#day-66>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Working with Props & TypeScript**; exact section title: **React + TypeScript** (14:20).
- Fit: partial foundation. The lesson supports typed props, not this API client’s cookie/CSRF behavior. Work narrow TypeScript request/response types and explicit fetch credentials/CSRF handling; keep calls on the same-origin local route.
- Focused worked references: [TypeScript everyday types — object types](https://www.typescriptlang.org/docs/handbook/2/objects.html); [Fetch API — credentials and response status](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).
- Video viewing: 14:20 at 1×; the remaining 10:40 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner-owned changed paths in `evidence/continuation-57-70/day-66.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
