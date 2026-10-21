# Day 48 — 2026-12-06: Add owner-scoped PostgreSQL renewal projection

## Contract

**Learner-owned seam:** `JdbcPolicyRenewalReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

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

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 04 - Getting Started with Spring JDBC**; exact section title: **Getting Started with JPA and Hibernate with Spring and Spring Boot** (3:58).
- Exact lesson title: **Step 07 - Querying Data using Spring JDBC**; exact section title: **Getting Started with JPA and Hibernate with Spring and Spring Boot** (5:36).
- Fit: partial foundation. The lessons introduce Spring JDBC, not the repository ownership invariant. Worked supplement: one parameterized SELECT with both policy ID and owner ID in the predicate; project stays on PostgreSQL/JDBC, not the video H2/JPA setup.
- Focused worked references: [Spring JdbcTemplate reference — query with parameters and a RowMapper](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html).
- Video viewing: 9:34 at 1×; the remaining 15:26 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Add owner-scoped PostgreSQL renewal projection: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-48.json`. Record first-attempt and review dates only after the work is done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-12-06 --roadmap-day 48` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
