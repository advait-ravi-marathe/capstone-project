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

## Interview transfer (inside the current evidence block)

**Question:** Could another engineer reproduce the result? What state can reset safely destroy?

**Observable evidence:** Rerun clean setup, name pinned versions/config and disposable-data guard; label unexecuted setup steps unverified.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Amazon official documentation/operation expectations](../interview-evidence-overlay.md#amazon official documentation/operation expectations) (anecdotal). These sources motivate practice only; they do not establish question frequency or predict an interview.
