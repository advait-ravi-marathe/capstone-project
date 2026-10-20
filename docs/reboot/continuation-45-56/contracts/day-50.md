# Day 50 — 2026-12-08: Map unavailable previews to stable HTTP errors

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewController.unavailable`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Conceal missing and foreign policy rows with the same 404 POLICY_NOT_FOUND response. For a found but non-eligible policy return 409 with code RENEWAL_PREVIEW_UNAVAILABLE and the fixed safe message in the acceptance test.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-50.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/renewal/PolicyRenewalControllerAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 11 - Implementing Exception Handling - 404 Resource Not Found**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (7:49).
- Exact lesson title: **Step 12 - Implementing Generic Exception Handling for all Resources**; exact section title: **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (11:29).
- Fit: partial foundation. The course examples are not this application’s error contract. Work a fixed mapping table for unavailable/foreign=404, infrastructure failure=sanitized 503, and unexpected failure=generic 500; never expose exception text.
- Focused worked references: [Spring MVC controller return types — ResponseEntity and response status/body](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html).
- Video viewing: 19:18 at 1×; the remaining 5:42 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Map unavailable previews to stable HTTP errors: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-50.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Use the actual-attempt queue first; the forecast anchors below are conditional and are not completion evidence.

- LC 2 D+7: forecast 2026-12-08 from Day 43 only if its first attempt occurred on schedule.
- LC 15 D+3: forecast 2026-12-08 from Day 47 only if its first attempt occurred on schedule.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-08`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
