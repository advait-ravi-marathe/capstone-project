# Day 11 — 2026-11-03: Reject stale quote selection

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

Boundary and stale-version negative tests. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day11ContractTest test`

This selector is installed and runs the fixed Day 11 cases. It currently fails at the explicit freshness-policy TODO; that is not a passing learner result. A callback verifies rejected requests do not reach a persistence action, but this is not a PostgreSQL integration test. Infrastructure checks are separate.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
