# Day 51 — 2026-12-09: Expose a minimal non-binding preview response

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewController.available`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

For available preview return HTTP 200 with exactly the public PolicyRenewalPreview projection. ResponseEntity controls the full HTTP response (including status, headers and body); the DTO and its contract tests define which fields are exposed. Do not expose owner, journey, quote-version IDs or mutation/acceptance fields.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-51.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/renewal/PolicyRenewalControllerAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51PublicPreviewIsMinimalAndContainsNoPrivateOrMutationFields test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

Read the named primary-source section for 10 minutes; spend 10 minutes sketching the invariant and layer boundary; use 5 minutes for a teach-back. These are planner allocations, not a course lecture count.

- [Official Spring guide: Create a Resource Representation Class and Create a Resource Controller](https://spring.io/guides/gs/rest-service/)
- [Official Spring Framework reference: ResponseEntity](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html) — ResponseEntity specifies status, headers and body; DTO field selection remains the application contract.

**Design sketch:** Expose a minimal non-binding preview response: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-51.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

Revisit: [LC 20 Valid Parentheses](https://leetcode.com/problems/valid-parentheses/) from the original Day 21 anchor (planned 2026-11-09). No attempt is recorded in the shared log. This is consolidation, not a new mandatory problem. If this is the first actual attempt, record it against Day 21; calculate D+3/D+7/D+21 from that actual date. If it was already attempted, keep the existing actual date and review clock.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-09`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
