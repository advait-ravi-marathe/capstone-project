# Day 66 — 2026-12-24: Build the normal React route and typed local API client

## Contract

**Learner-owned seam:** `LocalCoverStoryApp and localPolicyApi`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

The packaged root route `/` uses a typed same-origin client for demo-session selection, renewal preview, policy issue, policy readback and same-key replay. It presents loading, ready, unavailable and expired-session states accessibly; it contains no `/test/**` or `/__e2e/**` route.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.

**Acceptance test:** `frontend/src/__tests__/LocalCoverStoryApp.test.tsx`
**Acceptance selector:** `day66NormalRouteUsesTypedApiAndHandlesExpiredSession`
**Learner acceptance command:** `npm --prefix frontend run test:local`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Working with Props & TypeScript**; exact section title: **React + TypeScript** (14:20).
- Fit: partial foundation. The lesson supports typed props, not this API client’s cookie/CSRF behavior. Work narrow TypeScript request/response types and explicit fetch credentials/CSRF handling; keep calls on the same-origin local route.
- Focused worked references: [TypeScript everyday types — object types](https://www.typescriptlang.org/docs/handbook/2/objects.html); [Fetch API — credentials and response status](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).
- Video viewing: 14:20 at 1×; the remaining 10:40 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Build and test the normal React route and typed local API client at `/`; show which API call carries the owner session, CSRF token and unchanged replay request.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-66.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-24`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
