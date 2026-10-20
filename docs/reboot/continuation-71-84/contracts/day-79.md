# Day 79 — 2027-01-06: Wire one singleton gate around the existing route; DSA LC 230 Kth Smallest Element in a BST

## Contract

**Learner-owned seam:** `LocalRuntimeConfiguration.policyRenewalReadStore`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Use one app singleton around existing read adapter; finite Hikari timeout; no duplicate SQL.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewRuntimeWiringAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewRuntimeWiringAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

LocalRuntimeConfiguration.policyRenewalReadStore.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 03 - Exploring Java Spring Framework Bean Scopes - Prototype and Singleton**; exact section title: **Exploring Spring Framework Advanced Features** (9:17).
- Exact lesson title: **Step 04 - Comparing Prototype vs Singleton - Spring Framework Bean Scopes**; exact section title: **Exploring Spring Framework Advanced Features** (2:06).
- Fit: partial foundation. Trace one shared singleton gate around the existing adapter and a finite Hikari acquisition timeout. No duplicate SQL or request-scoped gate.
- Focused worked references: [Spring Framework bean scopes — singleton scope](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html).
- Video viewing: 11:23 at 1×; the remaining 13:37 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Trace singleton lifetime and controller → decorator → existing store.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-79.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

**DSA learner seam:** `src/main/java/assignments/dsa/KthSmallestInBst.java`
**DSA fixed acceptance:** `src/test/java/assignments/dsa/KthSmallestInBstAcceptanceTest.java`
**DSA command:** `./mvnw -Plearner-tests -Dtest=KthSmallestInBstAcceptanceTest test`

Run the queue first, then independently attempt [LC 230 Kth Smallest Element in a BST](https://leetcode.com/problems/kth-smallest-element-in-a-bst/). Planned first attempt: 2027-01-06; actual attempt is unset. If attempted on that date, D+3/D+7/D+21 are 2027-01-09, 2027-01-13, 2027-01-27; forecasts only.

- 25 minutes pattern learning
- 20 minutes independent solve
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-06`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
