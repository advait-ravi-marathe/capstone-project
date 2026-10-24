# Day 66 — 2026-12-28: Build the normal React route and typed local API client

## Contract

**Learner-owned seam:** `LocalCoverStoryApp and localPolicyApi`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The packaged root route `/` uses a typed same-origin client for demo-session selection, renewal preview, policy issue, policy readback and same-key replay. It presents loading, ready, unavailable and expired-session states accessibly; it contains no `/test/**` or `/__e2e/**` route.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.

**Acceptance test:** `frontend/src/__tests__/LocalCoverStoryApp.test.tsx`
**Acceptance selector:** `day66NormalRouteUsesTypedApiAndHandlesExpiredSession`
**Learner acceptance command:** `npm --prefix frontend run test:local`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Interview design mock (45 minutes inside the 85-minute practice block)

**Question:** [Q14 — Booking: retry after a lost response](../../interview/question-bank.md#q14)

Design a retry after a booking write succeeds but its response is lost. Distinguish the frozen user intent and idempotency key from transport retries, define visible pending/unknown/confirmed states, and explain why a retry must reuse the same request. Transfer to the existing local UI; do not add booking behavior.

Save the initial proposal before opening hints, worked references, or AI. Use Requirements 5 minutes; Capacity and constraints 5; Initial architecture and data model 15; Bottlenecks, reliability, scaling 10; Trade-offs, failures, follow-ups 10. Use the [100-point rubric](../../interview/mock-rubric.md).

**Remaining task slice (40 minutes):**

- 5 minutes: preconditions and prediction.
- 25 minutes: Implement one coherent learner-owned typed same-origin session-plus-preview vertical slice with explicit response decoding and owner/session state clearing. Use only existing API shapes; no issue POST from a React effect and no new retry mechanism.
- 10 minutes: run the named selector and record its result or first blocker. The full day acceptance above remains unchanged; unmet work stays open as **S5-D66-UI**.

## Selector limits and open evidence

- The available test:local suite is not a separate Sprint 5 selector and includes later behavior. Component tests alone do not prove the packaged browser/JAR/PostgreSQL flows; Day 66 remains open until all original required flows pass.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Working with Props & TypeScript**; exact section title: **React + TypeScript** (14:20).
- Fit: partial foundation. The lesson supports typed props, not this API client’s cookie/CSRF behavior. Work narrow TypeScript request/response types and explicit fetch credentials/CSRF handling; keep calls on the same-origin local route.
- Focused worked references: [TypeScript everyday types — object types](https://www.typescriptlang.org/docs/handbook/2/objects.html); [Fetch API — credentials and response status](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).
- Video viewing: 14:20 at 1×; the remaining 10:40 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-66.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
