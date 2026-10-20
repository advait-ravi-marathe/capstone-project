# Day 33 — 2026-11-21: Immutable issue snapshot

## Contract

**Learner-owned seam:** `PolicyIssueSnapshot`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes total: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). The evidence and teach-back fit inside these allocations; no setup task is added.

### Expected behavior

Persist the selected quoteVersionId, productVersion, rateVersion, annual premium, currency and effective date as an immutable issue snapshot; later catalog changes do not reprice it.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-33.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day33StoredIssueSnapshotDoesNotRepriceWhenCatalogChanges`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day33StoredIssueSnapshotDoesNotRepriceWhenCatalogChanges test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 15 - Exploring Java New Features - Records**; exact section title: **Java New Features - Java 10 to Java 24** (9:04).
- Exact lesson title: **3 Questions - Best practices with Records**; exact section title: **Core Java in Depth - Java Review (Interview) Guide** (9:58).
- Fit: partial foundation. Records support immutable snapshots, but persistence freeze and no-repricing need a project example; Java records are only shallowly immutable.
- Focused worked references: [Java 21 Record API — Record components and shallow immutability](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html).
- Video viewing: 19:02 at 1×; the remaining 5:58 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Immutable issue snapshot — sketch the invariant, enforcing layer, and failure response.

**Teach-back:** Explain one invariant, where it is enforced, and what test/response demonstrates it.

## Evidence allocation (10 minutes)

Record command and result in `evidence/continuation-31-44/day-33.json`. Include one short teach-back (or one architecture/ADR sketch) and answer one prompt: “What invariant does this change protect, where is it enforced, and what observable response proves it?” Include actual DSA attempt/review dates only after doing the work; forecasts are not completion evidence.

## DSA block (60 minutes)

New supplemental bridge problem (not verified as a Top Interview 150 plan member): [LeetCode 206 Reverse Linked List](https://leetcode.com/problems/reverse-linked-list/) (pointer rewiring). First attempt planned 2026-11-21; forecast reviews 2026-11-24, 2026-11-28, 2026-12-12 only if attempted as planned. Recompute from the learner's actual first attempt.

- 25 minutes pattern study/retrieval
- 20 minutes independent attempt or cold review
- 15 minutes active recall and error log

Before taking a new item, compute the cross-batch queue from actual first-attempt dates. Review due items by cold recall and re-solving, not rereading. If a review fails, use the same 60-minute block to repair it and displace new work; do not add minutes. The Days 1–30 anchors are carried with unknown actual dates until the learner records them.

Guide: [Top 150 continuation map](../dsa/top-150-map.md). Do not mark a review complete from its forecast date alone.

**Review-queue command:** `python3 scripts/dsa-review-queue.py --on-date 2026-11-21`
**Representative queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py`
