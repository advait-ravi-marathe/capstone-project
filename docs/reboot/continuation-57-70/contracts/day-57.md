# Day 57 — 2026-12-15: Gate the local assembly on prior learner evidence

## Contract

**Learner-owned seam:** `LocalReadinessGate.assess`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

A readiness check accepts only actual evidence for required prior milestones. If anything is missing or failed, today becomes a recovery block; it does not add work or imply completion.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.
Fixed acceptance tests verify the observable contract and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Until the named learner seam is completed, the learner selector is expected to fail at that explicit TODO. Instructor infrastructure checks are separate and should pass. Record the real command, exit code, output, TODO reached and learner-owned paths in `evidence/continuation-57-70/day-57.json`. A green infrastructure harness does not prove learner behavior.

**Acceptance test:** `src/test/java/assignments/local/LocalReadinessGateAcceptanceTest.java`
**Acceptance selector:** `day57MissingOrFailedPriorEvidenceReplacesNewWork`
**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day57MissingOrFailedPriorEvidenceReplacesNewWork test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

Read the named primary source for 10 minutes; spend 10 minutes sketching the trust/data boundary; use 5 minutes for a teach-back. These are planner allocations, not a lecture count.

- [GitHub Actions workflow syntax — job dependencies and conditional jobs — needs dependencies and status-check conditions](https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions)

**Design sketch:** Gate the local assembly on prior learner evidence: show the key trust boundary or data invariant and which layer owns it.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-57.json`. Leave completion and actual dates blank until the work is actually done.

## Separate Java DSA block (60 minutes)

No mandatory new problem. Run the shared actual-attempt queue first. Work on due reviews from actual first-attempt dates; if none are due, use cold retrieval and error-log repair from previous patterns. Do not infer attempts or completions from planned dates.

- 25 minutes focused pattern learning or recall
- 20 minutes independent attempt or due cold review
- 15 minutes active retrieval and error log

Run the shared queue before selecting work. D+3/D+7/D+21 use actual first-attempt dates from [the shared log](../../continuation-31-44/dsa/actual-attempt-log.json). A failed review replaces new work inside the same 60-minute block. See [the tree study map](../dsa/tree-study-map.md).

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2026-12-15`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py`
