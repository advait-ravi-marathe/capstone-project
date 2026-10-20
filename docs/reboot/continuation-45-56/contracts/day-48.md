# Day 48 — 2026-12-06: Add owner-scoped PostgreSQL renewal projection

## Contract

**Learner-owned seam:** `JdbcPolicyRenewalReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Use a parameterized SELECT constrained by both policy_id and owner_id. Return only policy ID, effective date, status, policy version, product/rate versions, current annual premium and currency. Missing/foreign rows return Optional.empty; query must not write.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-48.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/insurance/PolicyRenewalRepositoryAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyRenewalRepositoryAcceptanceTest#day48ReadIsOwnerScopedAndReturnsOnlyRenewalInputsWithoutWrites test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

Read the named primary-source section for 10 minutes; spend 10 minutes sketching the invariant and layer boundary; use 5 minutes for a teach-back. These are planner allocations, not a course lecture count.

- [Official reference: Add owner-scoped PostgreSQL renewal projection — parameter binding and scoped query execution](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html)

**Design sketch:** Add owner-scoped PostgreSQL renewal projection: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-48.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Use the actual-attempt queue first; the forecast anchors below are conditional and are not completion evidence.

- LC 128 D+3: forecast 2026-12-06 from Day 45 only if its first attempt occurred on schedule.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-06`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
