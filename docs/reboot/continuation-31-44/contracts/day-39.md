# Day 39 — 2026-11-27: Optimistic version conflict

## Contract

**Learner-owned seam:** `PolicyLifecycleService.endorseAtExpectedVersion(dataSource, policy, request)`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes total: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). The evidence and teach-back fit inside these allocations; no setup task is added.

### Expected behavior

Two synchronized writers with expected version 1 race against persisted version 1. Exactly one accepted write wins and the loser returns `VersionConflictException`. PostgreSQL must contain version 2 and only the issue event plus the winning endorsement event—no partial loser row.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-39.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day39StaleConcurrentEndorsementDoesNotCreatePartialVersion`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day39StaleConcurrentEndorsementDoesNotCreatePartialVersion test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

Use this focused sequence: **10 minutes** read the named official documentation section(s) below; **10 minutes** sketch the design decision for this day's contract; **5 minutes** explain one invariant and its test to yourself. The sketch and teach-back fit inside this 25-minute allocation. No course lecture number is asserted. The AWS exam objective is a secondary curriculum alignment label only; this is local Java/Spring/PostgreSQL/React study, with no cloud setup.

- [PostgreSQL 14 transaction isolation — Section 13.2.1, Read Committed](https://www.postgresql.org/docs/14/transaction-iso.html)

**Design sketch:** Optimistic version conflict — sketch the invariant, enforcing layer, and failure response.

**Teach-back:** Explain one invariant, where it is enforced, and what test/response demonstrates it.

## Evidence allocation (10 minutes)

Record command and result in `evidence/continuation-31-44/day-39.json`. Include one short teach-back (or one architecture/ADR sketch) and answer one prompt: “What invariant does this change protect, where is it enforced, and what observable response proves it?” Include actual DSA attempt/review dates only after doing the work; forecasts are not completion evidence.

## DSA block (60 minutes)

New Top 150 anchor: [LeetCode 19 Remove Nth Node From End of List](https://leetcode.com/problems/remove-nth-node-from-end-of-list/) (lead/lag pointers). First attempt planned 2026-11-27; forecast reviews 2026-11-30, 2026-12-04, 2026-12-18 only if attempted as planned. Recompute from the learner's actual first attempt.

- 25 minutes pattern study/retrieval
- 20 minutes independent attempt or cold review
- 15 minutes active recall and error log

Before taking a new item, compute the cross-batch queue from actual first-attempt dates. Review due items by cold recall and re-solving, not rereading. If a review fails, use the same 60-minute block to repair it and displace new work; do not add minutes. The Days 1–30 anchors are carried with unknown actual dates until the learner records them.

Guide: [Top 150 continuation map](../dsa/top-150-map.md). Do not mark a review complete from its forecast date alone.

**Review-queue command:** `python3 scripts/dsa-review-queue.py --on-date 2026-11-27`
**Representative queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py`
