# Day 11 — 2026-10-30: Reject stale quote selection

**State:** Contract defined; isolated policy scaffold and learner acceptance tests are installed. Learner attempt is pending; the focused suite is expected to fail at its explicit TODO.

**Focused official supplement:** No exact verified preferred-course lecture covers this pure freshness guard. Read Java 21 [`Instant.isBefore`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html), then the [worked owner/expiry/version ordering guide](../teaching/day-11-freshness-order.md). The earlier 19:16 isolation-level video bundle is not assigned today; it does not teach this predicate. Total: 5 minutes reading + 16 minutes worked cases + 4 minutes readiness = 25 minutes.

## Learner seam

`QuoteFreshnessPolicy`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Only the journey’s current unexpired version can be newly selected. Expired-at-now returns HTTP 409 `QUOTE_EXPIRED`; superseded returns HTTP 409 `QUOTE_VERSION_STALE`; quote from another journey returns HTTP 404 `RESOURCE_NOT_FOUND`. Each rejection writes zero rows.

## Required edge cases

Current, expired-at-now, superseded, wrong journey, and no-write checks.

## Prerequisite and isolation

Days 7 and 9–10; fixed current-version metadata and Clock. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Readiness question (inside the existing evidence slot):** What is checked first, and why? What does a zero callback count prove—and not prove? Name the check/write race without designing a database CAS or lock.

## Concept (25 minutes)

Immutable freshness metadata, strict expiry equality, and the fixed journey → expiry → version rejection order. `now.isBefore(expiresAt)` rejects equality. The worked example precedes the policy task; it does not teach or require SQL compare-and-set.

## Evidence (10 minutes)

Boundary and stale-version negative tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day11ContractTest test`

This selector is installed and runs the fixed Day 11 cases. It currently fails at the explicit freshness-policy TODO; that is not a passing learner result. A callback verifies rejected requests do not reach a persistence action, but this is not a PostgreSQL integration test. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Which rejection wins when the record is expired and also superseded?

**Observable evidence:** Predict owner/expiry/version outcomes, show that rejected cases invoke no callback, and state that this pure guard cannot make checking and writing atomic.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A4](../interview-evidence-overlay.md#a4) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 11` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
