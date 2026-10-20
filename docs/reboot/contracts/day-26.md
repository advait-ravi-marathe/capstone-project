# Day 26 — 2026-11-14: Record minimal selection audit

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

audit migration/write service. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

First successful selection appends exactly `{action:"SELECTION_CREATED", resourceId, actorId, occurredAt}` in the same transaction; rollback removes both selection and audit; a repeated request appends no creation event. Store no request or quote payload.

## Required edge cases

Success, repeat, rollback; exact event count/fields; no payload column.

## Prerequisite and isolation

Days 10, 12 and 25; fixed actor/time and seeded input. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Atomic audit and data minimization.

## Evidence (10 minutes)

Success/repeat/rollback database counts. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day26ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 290 Word Pattern. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
