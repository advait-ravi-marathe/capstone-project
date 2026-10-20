# Day 54 — 2026-12-12: Verify the owner-scoped preview against PostgreSQL

## Contract

**Learner-owned seam:** `GET /api/policies/{policyId}/renewal-preview`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

A MockMvc Spring MVC request backed by embedded PostgreSQL returns 200 and the next-term date for the owner; another owner receives concealed 404. Both requests leave policy row count unchanged.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-54.json`. A MockMvc contract is an in-process application test, not a real HTTP server/browser check. Days 55–56 own the later real-server/browser integration gate. A contract or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/renewal/PolicyRenewalPostgresAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyRenewalPostgresAcceptanceTest#day54OwnerScopedEndpointReadsPostgresAndLeavesPolicyRowsUnchanged test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Java Unit Testing with Spring Boot & Mockito](https://www.udemy.com/course/learn-unit-testing-with-spring-boot/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 15 - Writing an Integration Test using @SpringBootTest**; exact section title: **Section 2 - Unit Testing With Spring Boot And Mockito** (8:51).
- Fit: partial foundation. The lesson title supports Spring integration-test context only. The instructor harness supplies PostgreSQL; walk one real owner-scoped read plus before/after database snapshots and prove no write. Do not swap in the course H2/JPA stack.
- Focused worked references: [Spring Boot application testing — testing with a running server and application context](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html).
- Video viewing: 8:51 at 1×; the remaining 16:09 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Verify the owner-scoped preview against PostgreSQL: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-54.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Use the actual-attempt queue first; the forecast anchors below are conditional and are not completion evidence.

- LC 15 D+7: forecast 2026-12-12 from Day 47 only if its first attempt occurred on schedule.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-12`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
