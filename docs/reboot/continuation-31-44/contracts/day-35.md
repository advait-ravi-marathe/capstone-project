# Day 35 — 2026-11-23: Atomic PostgreSQL issuance

## Contract

**Learner-owned seam:** `PolicyLifecycleService.persistIssueAtomically(dataSource, request, authenticatedOwnerId)`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes total: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). The evidence and teach-back fit inside these allocations; no setup task is added.

### Expected behavior

In one transaction, insert policy snapshot and idempotency receipt; uniqueness conflict rolls back both writes; verify with disposable embedded PostgreSQL.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-35.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance commands:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt test` and `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35ReceiptConflictRollsBackPolicyAndEvent test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Hibernate and JPA with Spring Boot in 100 Steps](https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 58 - Introduction to Transaction Management**; exact section title: **Transaction Management** (4:43).
- Exact lesson title: **Step 63 - Implementing Transaction Management - 3 Things to Decide**; exact section title: **Transaction Management** (4:38).
- Fit: partial foundation. Transaction concepts only; keep the JDBC stack. Trace two PostgreSQL writes and show the first insert rolls back when the receipt insert conflicts.
- Focused worked references: [PostgreSQL 14 tutorial: transactions — Section 3.4, transactions](https://www.postgresql.org/docs/14/tutorial-transactions.html); [JDBC transactions tutorial — Commit and rollback boundaries](https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html).
- Video viewing: 9:21 at 1×; the remaining 15:39 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Atomic PostgreSQL issuance — sketch the invariant, enforcing layer, and failure response.

**Teach-back:** Explain one invariant, where it is enforced, and what test/response demonstrates it.

## Evidence allocation (10 minutes)

Record command and result in `evidence/continuation-31-44/day-35.json`. Include one short teach-back (or one architecture/ADR sketch) and answer one prompt: “What invariant does this change protect, where is it enforced, and what observable response proves it?” Include actual DSA attempt/review dates only after doing the work; forecasts are not completion evidence.

## DSA block (60 minutes)

New Top 150 anchor: [LeetCode 21 Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/) (two-pointer merge). First attempt planned 2026-11-23; forecast reviews 2026-11-26, 2026-11-30, 2026-12-14 only if attempted as planned. Recompute from the learner's actual first attempt.

- 25 minutes pattern study/retrieval
- 20 minutes independent attempt or cold review
- 15 minutes active recall and error log

Before taking a new item, compute the cross-batch queue from actual first-attempt dates. Review due items by cold recall and re-solving, not rereading. If a review fails, use the same 60-minute block to repair it and displace new work; do not add minutes. The Days 1–30 anchors are carried with unknown actual dates until the learner records them.

Guide: [Top 150 continuation map](../dsa/top-150-map.md). Do not mark a review complete from its forecast date alone.

**Review-queue command:** `python3 scripts/dsa-review-queue.py --on-date 2026-11-23`
**Representative queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py`
