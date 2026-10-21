# Day 60 — 2026-12-18: Wire owner-scoped policy reads and prove preview performs no writes

## Contract

**Learner-owned seam:** `JdbcPolicyReadStore.findByPolicyIdAndOwnerId`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

Implement the actual JdbcPolicyReadStore.findByPolicyIdAndOwnerId seam. Map every approved current-row field, including both null and non-null cancellation dates; bind both policy ID and owner ID as SQL parameters. Same-owner reads return the expected changed fixture; foreign and absent IDs are empty. Compare complete ordered policy/event/idempotency rows after every read.

## Acceptance and expected TODO state

The fixed selector is installed. It currently reaches the explicit Day 48 owner-scoped renewal-read TODO; this is an expected upstream blocker, not a completed learner result.

**Acceptance test:** `src/test/java/assignments/local/LocalApplicationAcceptanceTest.java`
**Acceptance selector:** `day60OwnerScopedPreviewReadDoesNotWrite`
**Learner acceptance command:** `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest#day60OwnerScopedPreviewReadDoesNotWrite test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**

- Optional replacement only: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — **Step 07 - Querying Data using Spring JDBC**, section **Getting Started with JPA and Hibernate with Spring and Spring Boot** (5:36, item `35019858`); access `confirmed_owned`. Do not purchase or enroll; replace equal study time if already accessible.

**Study sequence (seconds; total 1500):**
- retrieve prior raw-JDBC teaching: 300 seconds.
- complete nullable loan snapshot and direct-port example: 960 seconds.
- prediction: 240 seconds.

**Official references:**
- [Java SE21: PreparedStatement](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html) — setString(int,String), executeQuery()
- [Java SE21: ResultSet](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/ResultSet.html) — getDate(String), getBigDecimal(String)

**Worked guide:** [day-60.md](../worked/day-60.md). Read the complete supplied reference before applying the distinct native variation. This is instructor teaching, not learner completion.

**Time rule:** Optional lessons replace time inside these 1500 seconds; pauses do too. No overtime. Access and viewing are not inferred.

**Runnable acceptance commands:**
- `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest#day60OwnerScopedPreviewReadDoesNotWrite test`

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-60.json`. Leave completion and actual dates blank until the work is actually done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 60` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
