# Day 12 — 2026-10-31: Enforce owner-scoped access

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

Owner/other/anonymous matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Dtest=Day12SecurityHarnessTest test` checks the untagged form-login, authentication, CSRF and protected-route harness cases. `./mvnw -Plearner-tests -Dtest=Day12ContractTest,Day12SecurityHarnessTest test` includes the tagged owner-policy learner case, which remains expected red at its TODO.

The untagged harness cases verify only the instructor test filter chain. Its anonymous 401 and protected-route behavior are expected outcomes of this isolated harness, not learner achievement or production security evidence. The tagged owner-scoped read remains blocked at the learner policy TODO until it is implemented and the tagged selector passes.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 383 (D+7 from Day 5), LC 238 (D+3 from Day 9). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Who supplies identity? Why is filtering by a client owner ID insufficient?

**Observable evidence:** Trace trusted principal to scoped query; demonstrate owner/other/anonymous outcomes; explain authentication versus authorization.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
