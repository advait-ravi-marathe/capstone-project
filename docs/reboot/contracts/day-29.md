# Day 29 — 2026-11-17: Make the slice reproducible

**State:** Safety/refusal tests and a positive reset integration test on fresh embedded PostgreSQL are installed. `--embedded-test --confirm-disposable` needs no psql or remote target; external URI reset remains explicitly guarded.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/); section “Spring Boot: Getting Started with Spring Boot”. Watch: “Step 09 - Get Production Ready with Spring Boot - 1 - Profiles” (7:05); “Step 10 - Get Production Ready with Spring Boot - 2 - ConfigurationProperties” (7:31); “Step 11 - Get Production Ready with Spring Boot - 3 - Embedded Servers” (6:27). 21:03 viewing. Add the project’s disposable-reset safety walkthrough; videos do not authorize destructive reset or cloud resources. See the [Days 1–30 map](../course-learning-map.md) for course verification and the total-time rule.

## Learner seam

README/runbook plus deterministic fixture reset command. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Add `scripts/reset-synthetic-homesecure.sh` (invocation: `./scripts/reset-synthetic-homesecure.sh --database-url "$TEST_DATABASE_URL" --confirm-disposable`) and document clean setup: full JDK 21, pinned Maven wrapper, disposable PostgreSQL, no secrets, required tests and recovery. This reset is test-only and must target a disposable database; it accepts only a DB name ending `_test` and requires `--confirm-disposable`. Otherwise it exits nonzero before connecting or deleting data. It restores exact synthetic fixture state. Never point it at a development, shared, staging, or production database.

## Required edge cases

Reset disposable DB and verify exact seed; missing confirmation and DB name without `_test` both refuse before destructive SQL.

## Prerequisite and isolation

Days 13 and 28; fresh disposable test-only environment. Reset must never target development, shared, staging, or production data. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Map one-command setup/reset lifecycle and identify the disposable-data safety boundary. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Configuration, environment parity, safe operations.

## Evidence (10 minutes)

Another clean setup or exact unverified steps/blocker. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Dtest=Day29ResetIntegrationTest test`

The positive check resets twice in a newly started embedded PostgreSQL instance and verifies the exact synthetic rows. The shell command is `./scripts/reset-synthetic-homesecure.sh --embedded-test --confirm-disposable`; external URI mode still needs psql and a confirmed `*_test` target.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 153 Find Minimum in Rotated Sorted Array — rotated-array search. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Could another engineer reproduce the result? What state can reset safely destroy?

**Observable evidence:** Rerun clean setup, name pinned versions/config and disposable-data guard; label unexecuted setup steps unverified.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official). These references guide practice; they do not establish question frequency or predict interview content.
