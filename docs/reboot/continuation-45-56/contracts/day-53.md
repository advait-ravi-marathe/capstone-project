# Day 53 — 2026-12-11: Add accessible loading and unavailable states

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPanel`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Loading is exposed as an accessible status; unauthenticated and unavailable states are accessible alerts with the contract messages. Refresh invokes only the supplied read callback.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-53.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `frontend/src/__tests__/PolicyRenewalPreviewPanel.test.tsx`
**Acceptance command:** `cd frontend && npm run test:learner -- src/__tests__/PolicyRenewalPreviewPanel.test.tsx -t "explicit|refresh"`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Handling Loading States**; exact section title: **Sending Http Requests (e.g. Connecting to a Database)** (5:27).
- Exact lesson title: **Handling HTTP Loading & Error States**; exact section title: **Practice Project: Building a Food Order App** (3:12).
- Fit: partial foundation. The course examples do not establish this app’s accessible contract. Work one aria-live status for loading and one role=alert unavailable message; preserve a retry action only when the API operation is safe and explicit.
- Focused worked references: [WAI-ARIA status role — status messages](https://www.w3.org/TR/wai-aria-1.2/#status); [WAI-ARIA alert role — important dynamic messages](https://www.w3.org/TR/wai-aria-1.2/#alert).
- Video viewing: 8:39 at 1×; the remaining 16:21 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Add accessible loading and unavailable states: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-53.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

New anchor: [LC 150 Evaluate Reverse Polish Notation](https://leetcode.com/problems/evaluate-reverse-polish-notation/) (stack evaluation). First attempt planned for 2026-12-11; conditional D+3/D+7/D+21 forecasts are 2026-12-14, 2026-12-18, 2027-01-01. Recompute from the actual attempt date in the shared log; do not count the planned date as completion.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-11`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
