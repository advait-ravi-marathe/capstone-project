# Day 55 — 2026-12-13: Connect the React preview to Spring and PostgreSQL

## Contract

**Learner-owned seam:** `browser → React → GET renewal preview`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

A real browser loads the React preview through Spring and PostgreSQL for the authenticated synthetic owner. The rendered page shows premium/date/version/notice; response contains only approved snapshot fields and no renewal mutation.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-55.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/renewal/RenewalPreviewTransportInfrastructureTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day55RealBrowserLoadsOwnerScopedPreviewFromSpringAndPostgres test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Dtest=RenewalPreviewTransportInfrastructureTest#testOnlyReadOnlyBrowserTransportSurvivesSpringRestart test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux) — Academind / Maximilian Schwarzmüller; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Dynamic Query Functions & Query Keys**; exact section title: **React Query / Tanstack Query: Handling HTTP Requests With Ease** (13:05).
- Exact lesson title: **The Query Configuration Object & Aborting Requests**; exact section title: **React Query / Tanstack Query: Handling HTTP Requests With Ease** (5:27).
- Fit: partial foundation. TanStack Query lessons cover key/configuration and aborting requests; they do not prove this server’s cookie/CSRF or no-write contract. Work a stable owner-scoped preview key, explicit credentials/CSRF handling, and a no-mutation query; inspect only the project’s existing client setup.
- Focused worked references: [TanStack Query query keys — query-key identity and variables](https://tanstack.com/query/latest/docs/framework/react/guides/query-keys); [Fetch API — credentials and HTTP error handling](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).
- Video viewing: 18:32 at 1×; the remaining 6:28 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Connect the React preview to Spring and PostgreSQL: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-55.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

Revisit: [LC 155 Min Stack](https://leetcode.com/problems/min-stack/) from the original Day 23 anchor (planned 2026-11-11). No attempt is recorded in the shared log. This is consolidation, not a new mandatory problem. If this is the first actual attempt, record it against Day 23; calculate D+3/D+7/D+21 from that actual date. If it was already attempted, keep the existing actual date and review clock.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-13`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
