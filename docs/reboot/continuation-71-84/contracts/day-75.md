# Day 75 — 2027-01-02: Bound the existing owner-scoped read adapter

## Contract

**Learner-owned seam:** `BoundedPolicyRenewalReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner work not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Reject before delegate when full; preserve policy and owner IDs; release on success, empty and failure; wrap only CannotGetJdbcConnectionException.

### Acceptance and expected TODO state

The learner selector remains red only where an explicit learner TODO is incomplete. The Day 83 packaged-app integration selector is now installed and depends on the earlier learner seams; the contract-shape infrastructure test is not learner completion evidence.

**Learner acceptance test:** `src/test/java/assignments/renewal/BoundedPolicyRenewalReadStoreAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=BoundedPolicyRenewalReadStoreAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Work only this contract. Keep synchronous MVC/JDBC and a process-local admission gate of 2 with the existing Hikari maximum of 4; this arithmetic headroom does not reserve two pool slots or guarantee availability to other routes. No asynchronous execution, wait queue, blocking permit acquisition, automatic retry, added business endpoint, schema change, test-only fault route, or production test hook. Reuse the completed owner-scoped Day 48 SQL; do not duplicate or change its query. Use deterministic synchronization. Release test-owned locks and connections in `finally`.

BoundedPolicyRenewalReadStore.findByPolicyIdAndOwnerId.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 05 - Basics of Handling Exceptions - Need for finally**; exact section title: **Introduction to Exception Handling in Java** (6:23).
- Exact lesson title: **Step 11 - Throwing a Custom Exception - CurrenciesDoNotMatchException**; exact section title: **Introduction to Exception Handling in Java** (3:54).
- Fit: partial foundation. Trace permit release on success, empty result and failure. Translate only CannotGetJdbcConnectionException; unrelated exceptions must retain their meaning, never be swallowed.
- Focused worked references: [CannotGetJdbcConnectionException Javadoc — Class description and inheritance](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/CannotGetJdbcConnectionException.html).
- Video viewing: 10:17 at 1×; the remaining 14:43 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Trace exact exception types without parsing messages.

**Teach-back:** State the invariant, enforcing layer and observable evidence that proves it.

## Evidence allocation (10 minutes)

Record exact commands, exit status, observed values, learner-owned paths and blockers in `evidence/continuation-71-84/day-75.json`. Leave actual dates and learner completion blank until evidenced. Separate infrastructure results from TODO red results.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Review only anchors due from actual first-attempt dates; if none, use cold retrieval and error-log repair. Planned dates do not count as attempts or reviews. A failed due review displaces new work inside this 60-minute block.

- 25 minutes pattern learning or recall
- 20 minutes due cold review(s)
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-02`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
