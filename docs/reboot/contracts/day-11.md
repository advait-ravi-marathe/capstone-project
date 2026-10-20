# Day 11 — 2026-10-30: Reject stale quote selection

**State:** Contract defined; isolated policy scaffold and learner acceptance tests are installed. Learner attempt is pending; the focused suite is expected to fail at its explicit TODO.

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
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 11 (Reject stale quote selection) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
