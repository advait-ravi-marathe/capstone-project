# Day 47 — 2026-12-05: Freeze the renewal reference snapshot

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPolicy.buildPreview`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Copy the frozen source policy ID/version, product/rate versions, current annual premium and currency into a preview; derive only the next term effective date. Records provide shallow immutability: their component references are final, but referenced mutable objects are not frozen. Prefer immutable value types for snapshot components. Include an explicit reference-only notice stating this is not a renewal offer or coverage commitment. Do not calculate a new price or mutate state.

## Acceptance and expected TODO state

Fixed acceptance tests verify observable rules and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Before the named TODOs are completed, the selected learner test is expected to fail at that exact seam. Instructor infrastructure checks are separate and should pass. Record the exact command, exit code, assertions/errors and TODO reached in `evidence/continuation-45-56/day-47.json`. A contract, mock-only check, or infrastructure smoke does not claim product behavior is implemented.

**Acceptance test:** `src/test/java/assignments/insurance/PolicyRenewalAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day47PreviewCopiesCurrentSnapshotAndMakesNoRenewalPriceOrCommitment test`
**Instructor infrastructure test:** `src/test/java/assignments/renewal/RenewalPreviewManifestInfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Implementation/testing allocation (85 minutes)

Implement only this day's seam, run its fixed selector, and keep prior learner ownership intact. Use the synthetic fixture and fixed Clock; use isolated embedded PostgreSQL only where specified. No real customer data, payments, external services, migrations to a live database, or cloud setup.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Java Programming for Complete Beginners](https://www.udemy.com/course/java-programming-tutorial-for-beginners/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 15 - Exploring Java New Features - Records**; exact section title: **Java New Features - Java 10 to Java 24** (9:04).
- Exact lesson title: **3 Questions - Best practices with Records**; exact section title: **Core Java in Depth - Java Review (Interview) Guide** (9:58).
- Fit: partial foundation. Record guidance is only a foundation because record components can reference mutable objects. Work a preview snapshot containing immutable value types and prove later source changes do not alter it; preview never reprices or commits a renewal.
- Focused worked references: [Java 21 Record API — record components and shallow immutability](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html).
- Video viewing: 19:02 at 1×; the remaining 5:58 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Freeze the renewal reference snapshot: sketch the protected invariant and the layer that owns it.

**Teach-back:** State one invariant, its enforcing layer, and the observable response that proves it.

## Evidence allocation (10 minutes)

Record actual command/output, one design or teach-back note, and the learner's own changed paths in `evidence/continuation-45-56/day-47.json`. Record first-attempt and review dates only after the work is done.

## Separate Java DSA block (60 minutes)

New anchor: [LC 15 3Sum](https://leetcode.com/problems/3sum/) (sort, then move two pointers). First attempt planned for 2026-12-05; conditional D+3/D+7/D+21 forecasts are 2026-12-08, 2026-12-12, 2026-12-26. Recompute from the actual attempt date in the shared log; do not count the planned date as completion.

- 25 minutes pattern learning/retrieval
- 20 minutes independent first attempt or cold review
- 15 minutes active recall and error log

Run the shared queue before new work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [DSA map](../dsa/top-150-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-05`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
