# Day 34 — 2026-11-22: Issue endpoint and stable errors

## Contract

**Learner-owned seam:** `POST /api/policies`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes total: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). The evidence and teach-back fit inside these allocations; no setup task is added.

### Expected behavior

Expose issuance with 201 + Location on first creation, 200 on identical idempotent replay, 400 invalid request, 404 inaccessible quote, 409 key conflict; no success on learner TODO.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-34.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day34MapsCreateReplayAndConflictingReplayToStableHttpCodes`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day34MapsCreateReplayAndConflictingReplayToStableHttpCodes test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

Use this focused sequence: **10 minutes** read the named official documentation section(s) below; **10 minutes** sketch the design decision for this day's contract; **5 minutes** explain one invariant and its test to yourself. The sketch and teach-back fit inside this 25-minute allocation. No course lecture number is asserted. The AWS exam objective is a secondary curriculum alignment label only; this is local Java/Spring/PostgreSQL/React study, with no cloud setup.

- [Spring MVC controller return types — ResponseEntity and response status/body](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html)

**Design sketch:** Issue endpoint and stable errors — sketch the invariant, enforcing layer, and failure response.

**Teach-back:** Explain one invariant, where it is enforced, and what test/response demonstrates it.

## Evidence allocation (10 minutes)

Record command and result in `evidence/continuation-31-44/day-34.json`. Include one short teach-back (or one architecture/ADR sketch) and answer one prompt: “What invariant does this change protect, where is it enforced, and what observable response proves it?” Include actual DSA attempt/review dates only after doing the work; forecasts are not completion evidence.

## DSA block (60 minutes)

No mandatory new problem. Consolidate queued linked-list work; honor listed D+3/D+7/D+21 due reviews only when the anchor's actual attempt date makes them due. Record actual attempts, retrieval score, and one error-log correction.

- 25 minutes pattern study/retrieval
- 20 minutes independent attempt or cold review
- 15 minutes active recall and error log

Before taking a new item, compute the cross-batch queue from actual first-attempt dates. Review due items by cold recall and re-solving, not rereading. If a review fails, use the same 60-minute block to repair it and displace new work; do not add minutes. The Days 1–30 anchors are carried with unknown actual dates until the learner records them.

Guide: [Top 150 continuation map](../dsa/top-150-map.md). Do not mark a review complete from its forecast date alone.

**Review-queue command:** `python3 scripts/dsa-review-queue.py --on-date 2026-11-22`
**Representative queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py`
