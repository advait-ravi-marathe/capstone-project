# Day 46 — 2026-12-04: Enforce renewal window and lifecycle eligibility

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPolicy.eligibility`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Only ISSUED policies are renewable. Before the window opens return TOO_EARLY; include both the opening date and the day before the exclusive term end; on the term-end date return TERM_ENDED. CANCEL_SCHEDULED and CANCELLED return NOT_RENEWABLE.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-46.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/insurance/PolicyRenewalAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day46RenewalWindowHasFixedOpenCloseAndLifecycleBoundaries test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

Read the named primary-source section for 10 minutes; spend 10 minutes sketching the invariant and layer boundary; use 5 minutes for a teach-back. These are planner allocations, not a course lecture count.

- [Official reference: Enforce renewal window and lifecycle eligibility — fixed Clock and deterministic boundary tests](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html)

**Design sketch:** Enforce renewal window and lifecycle eligibility: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-46.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Use the actual-attempt queue first; the forecast anchors below are conditional and are not completion evidence.

- LC 2 D+3: forecast 2026-12-04 from Day 43 only if its first attempt occurred on schedule.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-04`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
