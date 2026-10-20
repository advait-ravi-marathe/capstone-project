# Day 70 — 2026-12-28: Complete the mastery, recovery and design mock

## Contract

**Learner-owned seam:** `Evidence review and local application design explanation`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Present the system boundary, trust/ownership flow, CSRF choice, migration lifecycle, no-write preview invariant, and recovery evidence. Report exact passing and failing selectors. No new feature or extra minutes is assigned.

## Acceptance and expected TODO state

The instructor integrity selector is installed and passed 3/3; it checks the evidence boundary only and is not a learner completion test. The learner task is to review the recorded results and complete the explain/design mock. No TODO failure is expected from this integrity selector.

**Acceptance test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Acceptance selector:** `day70EvidenceAndDesignMockStayWithinTheDefinedSlice`
**Instructor integrity command:** `./mvnw -Plearner-tests -Dtest=LocalApplicationManifestInfrastructureTest#day70EvidenceAndDesignMockStayWithinTheDefinedSlice test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: assessment review no new lecture. No new lecture. Use the existing 25-minute block to explain one observed boundary using actual selector evidence, then identify recovery work; the design mock is not evidence of implementation completion.
- Focused worked references: [C4 Software System — software system](https://c4model.com/abstractions/software-system); [C4 Container — container](https://c4model.com/abstractions/container); [C4 Component — component](https://c4model.com/abstractions/component).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.


**Design sketch:** Complete the mastery, recovery and design mock: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-70.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-28`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
