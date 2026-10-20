# Day 11 — 2026-10-30: Reject stale quote selection

**State:** Contract defined; isolated policy scaffold and learner acceptance tests are installed. Learner attempt is pending; the focused suite is expected to fail at its explicit TODO.

**Course lesson:** Conditional reference to [Master Hibernate and JPA with Spring Boot in 100 Steps — in28Minutes Official](https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps/), “Transaction Management”: “Step 60 - Understanding Dirty, Phanthom and Non Repeatable Reads” (9:25), “Step 61 - Understand 4 Isolation Levels” (5:48), and “Step 62 - Choosing between Isolation Levels” (4:03), 19:16 total. These cover isolation/stale-read concepts only, not optimistic locking/version checks; use the linked version-conflict example within the same 25-minute block. Course access is unconfirmed; no purchase/enrollment. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-11---version-check-versus-lost-update).

## Learner seam

`QuoteFreshnessPolicy`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Only the journey’s current unexpired version can be newly selected. Expired-at-now returns HTTP 409 `QUOTE_EXPIRED`; superseded returns HTTP 409 `QUOTE_VERSION_STALE`; quote from another journey returns HTTP 404 `RESOURCE_NOT_FOUND`. Each rejection writes zero rows.

## Required edge cases

Current, expired-at-now, superseded, wrong journey, and no-write checks.

## Prerequisite and isolation

Days 7 and 9–10; fixed current-version metadata and Clock. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show how current-version metadata is checked before the selection transaction. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Optimistic concurrency vs stale reads [G].

## Evidence (10 minutes)

Boundary and stale-version negative tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day11ContractTest test`

This selector is installed and runs the fixed Day 11 cases. It currently fails at the explicit freshness-policy TODO; that is not a passing learner result. A callback verifies rejected requests do not reach a persistence action, but this is not a PostgreSQL integration test. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 125 Valid Palindrome — inward pointers. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** If a version becomes stale between checking and writing, what assumption is missing?

**Observable evidence:** Identify the check/write race and current guarantees; show expired/stale negatives; explain a possible atomic database guard without implementing later scope early.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A4](../interview-evidence-overlay.md#a4) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
