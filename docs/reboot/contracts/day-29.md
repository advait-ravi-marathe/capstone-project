# Day 29 — 2026-11-17: Make the slice reproducible

**State:** Safety-gated reset script and refusal tests are installed. Positive reset verification remains blocked where psql or a disposable target database is unavailable.

## Learner seam

README/runbook plus deterministic fixture reset command. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Add `scripts/reset-synthetic-homesecure.sh` (invocation: `./scripts/reset-synthetic-homesecure.sh --database-url "$TEST_DATABASE_URL" --confirm-disposable`) and document clean setup: full JDK 21, pinned Maven wrapper, disposable PostgreSQL, no secrets, required tests and recovery. Reset accepts only a DB name ending `_test` and requires `--confirm-disposable`; otherwise it exits nonzero before connecting or deleting data. It restores exact synthetic fixture state.

## Required edge cases

Reset disposable DB and verify exact seed; missing confirmation and DB name without `_test` both refuse before destructive SQL.

## Prerequisite and isolation

Days 13 and 28; fresh disposable environment. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Map one-command setup/reset lifecycle and identify the disposable-data safety boundary. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Configuration, environment parity, safe operations.

## Evidence (10 minutes)

Another clean setup or exact unverified steps/blocker. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day29ContractTest test`

This selector verifies only fail-closed argument handling and no-connect refusals. It does not prove a successful reset against disposable PostgreSQL; psql and a disposable test database are required for that positive check. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 153 Find Minimum in Rotated Sorted Array — rotated-array search. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 29 (Make the slice reproducible) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
