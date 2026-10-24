# Day 29 — 2026-11-21: Make the slice reproducible

**State:** Safety/refusal tests and a positive reset integration test on fresh embedded PostgreSQL are installed. `--embedded-test --confirm-disposable` needs no psql or remote target; external URI reset remains explicitly guarded.

**Required course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), “Step 09 - Get Production Ready with Spring Boot - 1 - Profiles” (7:05). ConfigurationProperties (7:31) and Embedded Servers (6:27) are optional later references, not stacked. Use the [Day 29 reset safety guide](../teaching/day-29-reset-safety.md#guard-decision-examples) inside the existing 25-minute study block; videos do not authorize destructive reset or cloud resources.

## Learner seam

README/runbook plus deterministic fixture reset command. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Add `scripts/reset-synthetic-homesecure.sh` (invocation: `./scripts/reset-synthetic-homesecure.sh --database-url "$TEST_DATABASE_URL" --confirm-disposable`) and document clean setup: full JDK 21, pinned Maven wrapper, disposable PostgreSQL, no secrets, required tests and recovery. This reset is test-only and must target a disposable database; it accepts only a DB name ending `_test` and requires `--confirm-disposable`. Otherwise it exits nonzero before connecting or deleting data. It restores exact synthetic fixture state. Never point it at a development, shared, staging, or production database.

## Required edge cases

Reset disposable DB and verify exact seed; missing confirmation and DB name without `_test` both refuse before destructive SQL.

## Prerequisite and isolation

Days 13 and 28; fresh disposable test-only environment. Reset must never target development, shared, staging, or production data. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Interview-first task proposal (5 minutes; inside the 85-minute practice block)

Before code, hints, worked answers, or AI, write: State the disposable-database name and explicit confirmation guard, then predict the first observable refusal before opening the reset guide.

**Task boundary:** Use the existing embedded reset test and reset-safety script; never use external URI mode. Use the existing day's acceptance selector and preserve its full criteria. This short proposal does not replace acceptance or add time. Record the prediction and actual result in the existing evidence record.

**Design prompt (within the 25-minute concept block):** Which guards run before connection or destructive SQL, and why does a profile or database-name suffix alone not prove reset authority? Use the [worked guard table](../teaching/day-29-reset-safety.md#guard-decision-examples).

## Concept (25 minutes)

Configuration and reset safety, taught with the [Day 29 guard trace](../teaching/day-29-reset-safety.md#existing-safe-exercise). Required video 7:05; optional later videos are not part of today’s assigned time.

## Evidence (10 minutes)

Another clean setup or exact unverified steps/blocker. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Dtest=Day29ResetIntegrationTest test`

The positive check resets twice in its newly started embedded PostgreSQL instance and verifies the exact synthetic rows. If using the wrapper, it runs only that same embedded test: `./scripts/reset-synthetic-homesecure.sh --embedded-test --confirm-disposable`. Do not use external URI mode for this exercise.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
