# Day 12 — 2026-11-04: Enforce owner-scoped access

**State:** Installed: a real Spring Security test filter chain verifies form-login session authentication and CSRF token rejection/acceptance. The production owner policy and production security configuration remain learner TODOs. The owner-policy HTTP acceptance test is tagged and expected red until the policy is implemented.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/); section “Securing Spring Boot Applications with Spring Security”. Watch: “Step 21 - Understanding Spring Security Authentication” (4:17); “Step 22 - Exploring Spring Security Authorization” (9:47). 14:04 viewing. Use the distinction as background; use the linked object-owner guard example because general authorization does not teach object-level access control. See the [Days 1–30 map](../course-learning-map.md) for course verification and the total-time rule. Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-12--object-ownership-beside-the-real-security-test-fixture).

## Learner seam

authorization policy / security config. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Target learner behavior, not a passing learner result: anonymous protected-harness requests return 401; an authenticated owner can read that owner’s journey; another owner gets the Day 3 non-disclosing 404 envelope. The 200/404 owner-read outcomes remain expected until the tagged learner-policy case passes. Client-supplied `ownerId` is ignored and never establishes identity. The instructor filter-chain check uses a form-login test session, not HTTP Basic. No production-security completion is claimed.

## Required edge cases

Anonymous, own, other owner, forged owner field.

## Prerequisite and isolation

Days 3, 9–11; Spring test principals and fixed owned-resource fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Readiness question (inside the existing evidence slot):** Who supplies the trusted identity, and why is a request-body owner field not proof? Distinguish the actual test filter-chain check from the pure owner-policy decision and production wiring.

## Concept (25 minutes)

Authentication and authorization plus the existing fixture trace: `owner-a` reads its seeded journey; `owner-b` receives concealed 404; an owner value in the request does not change the principal. The course supplies the distinction; this concrete test walkthrough connects it to the seam without claiming production security is configured.

## Evidence (10 minutes)

## Planned isolated command

`./mvnw -Dtest=Day12SecurityHarnessTest test` checks the four untagged form-login, authentication, CSRF and protected-route harness cases. Separately, `./mvnw -Plearner-tests -Dtest=Day12SecurityHarnessTest#ownerScopedJourneyHttpReadUsesLearnerPolicy test` runs the tagged owner-policy learner case, which remains expected red at its TODO. The original `Day12ContractTest` selector remains unchanged.

The untagged harness cases verify only the instructor test filter chain. Its anonymous 401 and protected-route behavior are expected outcomes of this isolated harness, not learner achievement or production security evidence. The tagged owner-scoped read remains blocked at the learner policy TODO until it is implemented and the tagged selector passes.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
