# CAP-74 — Expose owned mock payment attempt status

**Saved day:** 033 · 15 November 2026 · **Primary ID:** CAP-74 · **Maximum allocation:** 150 minutes

## Purpose and boundary

Map an authoritative owner-scoped payment-attempt snapshot into a safe read-only status response. The endpoint contract is `GET /api/v1/payment-attempts/{attemptId}` with successful fields `attemptId`, `state`, `updatedAt`, `requestId`, and `nextAction`.

The exercise uses synthetic data and no provider integration. `PENDING`, `UNKNOWN`, `FAILED`, and `SUCCEEDED` are payment states; none means a policy is active or cover has been issued. An UNKNOWN operation must retain its existing reference for reconciliation. A status read never creates an attempt or charge.

## Files and ownership

- Learner seam: `src/main/java/coverstory/modules/payments/api/CAP74Seam.java`
- Learner acceptance: `src/test/java/coverstory/modules/payments/CAP74AcceptanceTest.java`
- Independent instructor witnesses: `src/test/java/coverstory/modules/payments/CAP74InstructorInfrastructureTest.java`
- Synthetic contract: `fixtures/coverstory/CAP-74/synthetic-v1.json`
- Saved source card: `docs/assignments/coverstory/saved-days/day-033-cap-74.md`

Implement only `CAP74Seam.getStatus`. Keep repository access owner-scoped in the owning Payments module. Do not edit the fixture oracle or instructor tests to force a pass. The snapshot's operation reference is retained internally for reconciliation and must never appear in this status response.

## Required behavior

1. A missing principal returns `401 UNAUTHORIZED` without reading an attempt.
2. Query by authenticated owner and attempt identifier together. A foreign attempt and an absent attempt return the identical `404 PAYMENT_ATTEMPT_NOT_FOUND` response.
3. An owned PENDING attempt returns `200`, preserving its payment state, timestamp, and request ID, with `nextAction=NONE`. It must not imply onboarding or active cover.
4. An owned UNKNOWN attempt returns `200` and `nextAction=RECONCILE`. Retain the same operation reference; do not create or charge another attempt.
5. Recognize only PENDING, UNKNOWN, FAILED, and SUCCEEDED. An impossible persisted state returns sanitized `500 PAYMENT_STATE_INVALID`, never a success-shaped 200.
6. The success body has exactly the five documented fields. Do not expose operation/provider references, payloads, secrets, or raw provider data.

## Checks

The independent instructor oracle runs without calling the learner seam:

```sh
mvn -q -Dtest=CAP74InstructorInfrastructureTest test
```

Run the saved behavior contract after implementing the TODO:

```sh
mvn -q -Plearner-tests -Dtest=CAP74AcceptanceTest test
```

Before implementation, the acceptance suite is expected to stop at the explicit learner TODO. Record that separately from instructor setup results. Both suites use only `fixtures/coverstory/CAP-74/synthetic-v1.json`; no payment provider or charge is invoked.

## Saved acceptance mapping

| Case | Input | Expected behavior |
|---|---|---|
| CAP-74-CS-01 | A-owned PENDING attempt | Safe 200 PENDING status; not onboarding or active cover |
| CAP-74-CS-02 | A-owned UNKNOWN attempt | 200 UNKNOWN with RECONCILE; same reference retained, no new charge |
| CAP-74-CS-03 | B-owned and absent IDs queried by A | Identical 404 status and error body |
| CAP-74-CS-04 | Anonymous principal or impossible stored state | 401 or sanitized 500 PAYMENT_STATE_INVALID; never false 200 |

## Interview note

Explain how mapping an uncertain timeout to FAILED could cause a duplicate payment, and why even confirmed payment is not evidence that cover was issued. The saved task is bounded to the status read and its focused tests; its 150-minute envelope is not a rehearsal result.
